package com.selodigital.exportacao;

import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Tribunal;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Gera os documentos de exportação sem interferir na lógica de consulta. */
public final class ExportadorConsulta {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private ExportadorConsulta() { }

    public static void exportarResultados(Component pai, Tribunal tribunal, List<ResultadoConsulta> resultados, boolean pdf) {
        File arquivo = escolherArquivo(pai, "resultados-" + tribunal.name().toLowerCase(), pdf);
        if (arquivo == null) return;
        try {
            if (pdf) criarPdfResultados(arquivo, tribunal, resultados); else criarXlsxResultados(arquivo, tribunal, resultados);
            sucesso(pai, arquivo);
        } catch (Exception ex) { erro(pai, ex); }
    }

    public static void exportarDetalhes(Component pai, DetalhesSelo detalhes, boolean pdf) {
        File arquivo = escolherArquivo(pai, "detalhes-" + numeroExibido(detalhes).replaceAll("[^A-Za-z0-9._-]", "_"), pdf);
        if (arquivo == null) return;
        try {
            if (pdf) criarPdfDetalhes(arquivo, detalhes); else criarXlsxDetalhes(arquivo, detalhes);
            sucesso(pai, arquivo);
        } catch (Exception ex) { erro(pai, ex); }
    }

    private static File escolherArquivo(Component pai, String nome, boolean pdf) {
        String extensao = pdf ? "pdf" : "xlsx";
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Exportar em " + extensao.toUpperCase());
        seletor.setSelectedFile(new File(nome + "." + extensao));
        seletor.setFileFilter(new FileNameExtensionFilter(extensao.toUpperCase() + " (*." + extensao + ")", extensao));
        if (seletor.showSaveDialog(pai) != JFileChooser.APPROVE_OPTION) return null;
        File arquivo = seletor.getSelectedFile();
        if (!arquivo.getName().toLowerCase(Locale.ROOT).endsWith("." + extensao)) arquivo = new File(arquivo.getAbsolutePath() + "." + extensao);
        if (arquivo.exists() && JOptionPane.showConfirmDialog(pai, "O arquivo já existe. Deseja substituí-lo?", "Confirmar substituição", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return null;
        return arquivo;
    }

    private static void criarPdfResultados(File arquivo, Tribunal tribunal, List<ResultadoConsulta> resultados) throws IOException {
        List<String> cabecalhos = cabecalhosResultados(tribunal);
        List<List<String>> linhas = new ArrayList<>();
        for (int i=0;i<resultados.size();i++) linhas.add(linhaResultado(i + 1, resultados.get(i), tribunal));
        try (PDDocument doc = new PDDocument()) {
            PdfPagina pagina = novaPagina(doc, tribunal, "Resultados da Consulta", "Quantidade de selos: " + resultados.size());
            desenharTabela(doc, pagina, cabecalhos, linhas);
            doc.save(arquivo);
        }
    }

    private static void criarPdfDetalhes(File arquivo, DetalhesSelo d) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PdfPagina pagina = novaPagina(doc, d.getTribunal(), "Detalhes do Selo", "Número do selo: " + numeroExibido(d));
            List<List<String>> linhas = new ArrayList<>();
            linhas.add(List.of("Tribunal", d.getTribunal().toString()));
            linhas.add(List.of("Número do Selo", numeroExibido(d)));
            linhas.add(List.of("Status", d.getStatus().getDescricao()));
            for (Map.Entry<String,String> e : d.getCampos().entrySet()) if (!(d.getTribunal() == Tribunal.TJPE && "Número do Selo".equals(e.getKey()))) linhas.add(List.of(e.getKey(), texto(e.getValue())));
            desenharTabela(doc, pagina, List.of("Campo", "Valor"), linhas);
            doc.save(arquivo);
        }
    }

    private static PdfPagina novaPagina(PDDocument doc, Tribunal tribunal, String titulo, String subtitulo) throws IOException {
        PDRectangle paisagem = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        PDPage page = new PDPage(paisagem); doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float w = page.getMediaBox().getWidth();
        cs.setNonStrokingColor(new Color(32, 66, 110)); cs.addRect(0, w > 0 ? page.getMediaBox().getHeight()-82 : 0, w, 82); cs.fill();
        BufferedImage logo = logo(tribunal);
        if (logo != null) { PDImageXObject img = PDImageXObject.createFromByteArray(doc, imagemBytes(logo), "logo"); float escala = Math.min(100f/logo.getWidth(), 56f/logo.getHeight()); cs.drawImage(img, 36, page.getMediaBox().getHeight()-69, logo.getWidth()*escala, logo.getHeight()*escala); }
        escrever(cs, titulo, 150, page.getMediaBox().getHeight()-42, 18, PDType1Font.HELVETICA_BOLD, Color.WHITE);
        String identificacao = tribunal == null ? "" : tribunal.toString();
        if (subtitulo != null && !subtitulo.isBlank()) {
            identificacao += "  |  " + subtitulo;
        }
        escrever(cs, identificacao, 150, page.getMediaBox().getHeight()-62, 9, PDType1Font.HELVETICA, Color.WHITE);
        escrever(cs, "Exportado em " + DATA.format(LocalDateTime.now()), 36, page.getMediaBox().getHeight()-103, 9, PDType1Font.HELVETICA, Color.DARK_GRAY);
        return new PdfPagina(page, cs, page.getMediaBox().getHeight()-122, tribunal, titulo, subtitulo);
    }

    private static void desenharTabela(PDDocument doc, PdfPagina inicial, List<String> headers, List<List<String>> linhas) throws IOException {
        PdfPagina p = inicial; float margem = 36, largura = p.page.getMediaBox().getWidth()-72, col = largura/headers.size();
        p.y = cabecalhoTabela(p.cs, headers, margem, p.y, col);
        for (List<String> linha : linhas) {
            List<List<String>> celulas = new ArrayList<>();
            int maiorQuantidadeLinhas = 1;

            for (int c = 0; c < headers.size(); c++) {
                String valor = c < linha.size() ? linha.get(c) : "";
                List<String> linhasCelula = quebrarTexto(
                        valor,
                        PDType1Font.HELVETICA,
                        8,
                        col - 8
                );
                celulas.add(linhasCelula);
                maiorQuantidadeLinhas = Math.max(
                        maiorQuantidadeLinhas,
                        linhasCelula.size()
                );
            }

            float altura = Math.max(
                    26,
                    maiorQuantidadeLinhas * 10 + 10
            );

            if (p.y - altura < 38) {
                p.cs.close();
                p = novaPagina(
                        doc,
                        p.tribunal,
                        p.tituloBase + " - Continuação",
                        p.subtitulo
                );
                p.y = cabecalhoTabela(p.cs, headers, margem, p.y, col);
            }

            for (int c = 0; c < headers.size(); c++) {
                p.cs.setStrokingColor(new Color(210,215,220));
                p.cs.addRect(margem + c * col, p.y - altura, col, altura);
                p.cs.stroke();

                float yTexto = p.y - 12;
                for (String textoLinha : celulas.get(c)) {
                    escrever(
                            p.cs,
                            textoLinha,
                            margem + c * col + 4,
                            yTexto,
                            8,
                            PDType1Font.HELVETICA,
                            Color.DARK_GRAY
                    );
                    yTexto -= 10;
                }
            }

            p.y -= altura;
        }
        p.cs.close();
    }
    private static float cabecalhoTabela(PDPageContentStream cs,List<String> h,float x,float y,float col)throws IOException { for(int i=0;i<h.size();i++){cs.setNonStrokingColor(new Color(232,238,245));cs.addRect(x+i*col,y-24,col,24);cs.fill();escrever(cs,h.get(i),x+i*col+4,y-16,8,PDType1Font.HELVETICA_BOLD,new Color(32,66,110));}return y-24; }
    private static void escrever(PDPageContentStream cs,String s,float x,float y,float tamanho,PDType1Font fonte,Color cor)throws IOException { cs.beginText();cs.setFont(fonte,tamanho);cs.setNonStrokingColor(cor);cs.newLineAtOffset(x,y);cs.showText(s.replace("\n"," ").replace("\r", ""));cs.endText(); }

    private static List<String> quebrarTexto(
            String texto,
            PDType1Font fonte,
            float tamanho,
            float larguraMaxima) throws IOException {

        List<String> resultado = new ArrayList<>();
        String valor = texto == null ? "" : texto.replace("\r", "");

        for (String paragrafo : valor.split("\n", -1)) {
            if (paragrafo.isEmpty()) {
                resultado.add("");
                continue;
            }

            StringBuilder linha = new StringBuilder();
            for (String palavra : paragrafo.split("\\s+")) {
                if (linha.length() == 0) {
                    adicionarPalavraQuebrada(resultado, linha, palavra, fonte, tamanho, larguraMaxima);
                    continue;
                }

                String candidata = linha + " " + palavra;
                if (largura(fonte, candidata, tamanho) <= larguraMaxima) {
                    linha.append(" ").append(palavra);
                } else {
                    resultado.add(linha.toString());
                    linha.setLength(0);
                    adicionarPalavraQuebrada(resultado, linha, palavra, fonte, tamanho, larguraMaxima);
                }
            }

            if (linha.length() > 0) {
                resultado.add(linha.toString());
            }
        }

        return resultado.isEmpty() ? List.of("") : resultado;
    }

    private static void adicionarPalavraQuebrada(
            List<String> resultado,
            StringBuilder linha,
            String palavra,
            PDType1Font fonte,
            float tamanho,
            float larguraMaxima) throws IOException {

        for (int indice = 0; indice < palavra.length(); indice++) {
            String candidata = linha.toString() + palavra.charAt(indice);
            if (linha.length() > 0
                    && largura(fonte, candidata, tamanho) > larguraMaxima) {
                resultado.add(linha.toString());
                linha.setLength(0);
            }
            linha.append(palavra.charAt(indice));
        }
    }

    private static float largura(
            PDType1Font fonte,
            String texto,
            float tamanho) throws IOException {

        return fonte.getStringWidth(texto) / 1000f * tamanho;
    }

    private static void criarXlsxResultados(File arq, Tribunal tribunal, List<ResultadoConsulta> resultados) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) { Sheet sh = wb.createSheet("Resultados"); criarTitulo(sh, "Resultados da Consulta - " + tribunal); List<String> h=cabecalhosResultados(tribunal); Row r=sh.createRow(2); estiloCabecalho(wb,r,h); for(int i=0;i<resultados.size();i++){Row row=sh.createRow(i+3);List<String> vals=linhaResultado(i+1,resultados.get(i),tribunal);for(int c=0;c<vals.size();c++)row.createCell(c).setCellValue(vals.get(c));} finalizarPlanilha(sh,h.size(),resultados.size()+2); try(var out=Files.newOutputStream(arq.toPath())){wb.write(out);} }
    }
    private static void criarXlsxDetalhes(File arq, DetalhesSelo d) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) { Sheet sh=wb.createSheet("Detalhes"); criarTitulo(sh,"Detalhes do Selo - " + d.getTribunal()); Row h=sh.createRow(2); estiloCabecalho(wb,h,List.of("Campo","Valor")); int n=3; n=campo(sh,n,"Tribunal",d.getTribunal().toString()); n=campo(sh,n,"Número do Selo",numeroExibido(d)); n=campo(sh,n,"Status",d.getStatus().getDescricao()); for(Map.Entry<String,String> e:d.getCampos().entrySet())if(!(d.getTribunal()==Tribunal.TJPE&&"Número do Selo".equals(e.getKey())))n=campo(sh,n,e.getKey(),texto(e.getValue())); finalizarPlanilha(sh,2,n-1); try(var out=Files.newOutputStream(arq.toPath())){wb.write(out);} }
    }
    private static int campo(Sheet s,int n,String a,String b){Row r=s.createRow(n);r.createCell(0).setCellValue(a);r.createCell(1).setCellValue(b);return n+1;}
    private static void criarTitulo(Sheet s,String t){s.createRow(0).createCell(0).setCellValue(t + " | Exportado em " + DATA.format(LocalDateTime.now()));}
    private static void estiloCabecalho(Workbook wb,Row r,List<String> h){CellStyle st=wb.createCellStyle();st.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());st.setFillPattern(FillPatternType.SOLID_FOREGROUND);Font f=wb.createFont();f.setBold(true);f.setColor(IndexedColors.WHITE.getIndex());st.setFont(f);for(int i=0;i<h.size();i++){Cell c=r.createCell(i);c.setCellValue(h.get(i));c.setCellStyle(st);}}
    private static void finalizarPlanilha(Sheet s,int colunas,int ultima){for(int i=0;i<colunas;i++)s.autoSizeColumn(i);s.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(2,ultima,0,colunas-1));s.createFreezePane(0,3);}

    private static List<String> cabecalhosResultados(Tribunal t){return switch(t){case TJPB,TJAL -> List.of("#","Número do Selo","Status","Matrícula/Protocolo","Tipo de Ato","Subtipo de Ato");case TJPE -> List.of("#","Número do Selo","Status","Ato");case TJRN -> List.of("#","Número do Selo","Status");};}
    private static List<String> linhaResultado(int n,ResultadoConsulta r,Tribunal t){List<String> l=new ArrayList<>(List.of(String.valueOf(n),numeroExibido(r),r.getStatus().getDescricao()));if(t==Tribunal.TJPB||t==Tribunal.TJAL){l.add(texto(r.getMatriculaProtocolo()));l.add(texto(r.getTipoAto()));l.add(texto(r.getSubtipoAto()));}else if(t==Tribunal.TJPE)l.add(texto(r.getCampos().get("Ato")));return l;}
    private static String numeroExibido(ResultadoConsulta r){return r.getTribunal()==Tribunal.TJPE?texto(r.getCampos().get("Número do Selo")):r.getSelo().getNumero();}
    private static String numeroExibido(DetalhesSelo d){return d.getTribunal()==Tribunal.TJPE?texto(d.getCampos().get("Número do Selo")):d.getSelo().getNumero();}
    private static String texto(String v){return v==null||v.isBlank()?"Não informado":v.trim();}
    private static String truncar(String v,int max){return v.length()>max?v.substring(0,max-1)+"…":v;}
    private static BufferedImage logo(Tribunal t)throws IOException{if(t==null)return null;var u=ExportadorConsulta.class.getResource("/logos/logo_"+t.name().toLowerCase()+".png");return u==null?null:ImageIO.read(u);}
    private static byte[] imagemBytes(BufferedImage i)throws IOException{var out=new java.io.ByteArrayOutputStream();ImageIO.write(i,"png",out);return out.toByteArray();}
    private static void sucesso(Component p,File f){JOptionPane.showMessageDialog(p,"Arquivo exportado com sucesso em:\n"+f.getAbsolutePath(),"Exportação concluída",JOptionPane.INFORMATION_MESSAGE);}
    private static void erro(Component p,Exception e){JOptionPane.showMessageDialog(p,"Não foi possível exportar o arquivo.\n\n"+e.getMessage(),"Erro de exportação",JOptionPane.ERROR_MESSAGE);}
    private static class PdfPagina{final PDPage page;final PDPageContentStream cs;final Tribunal tribunal;final String tituloBase;final String subtitulo;float y;PdfPagina(PDPage p,PDPageContentStream c,float y,Tribunal tribunal,String tituloBase,String subtitulo){page=p;cs=c;this.y=y;this.tribunal=tribunal;this.tituloBase=tituloBase.replace(" - Continuação", "");this.subtitulo=subtitulo;}}

    //endregion
}
