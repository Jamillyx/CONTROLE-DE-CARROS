package web.controlevacinacao.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class RelatorioService {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioService.class);

    private DataSource dataSource;

    public RelatorioService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public byte[] gerarRelatorioSimplesTodasVacinas() {
        InputStream arquivoJasper = getClass().getResourceAsStream("/relatorios/RelatorioSQLDiretoSimples.jasper");
        try (Connection conexao = dataSource.getConnection()) {
            try {
                JasperPrint jasperPrint = JasperFillManager.fillReport(arquivoJasper, null, conexao);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } catch (JRException e) {
                logger.error("Problemas na geracao do PDF do relatório: " + e);
            }
        } catch (SQLException e) {
            logger.error("Problemas na obtenção de uma conexão com o BD na geração de relatório: " + e);
        }

        return null;
    }

    public byte[] gerarRelatorioComplexoTodasVacinasLotes() {
        try (Connection conexao = dataSource.getConnection()) {
            try {
                ClassPathResource cpr = new ClassPathResource("relatorios/RelatorioSQLDiretoComplexoParametros.jasper");
                InputStream arquivoJasper = cpr.getInputStream();

                String urlRelatorio = cpr.getURL().toString();
                String diretorioRelatorios = urlRelatorio.substring(0, urlRelatorio.lastIndexOf("/") + 1);
                logger.debug("diretorioRelatorios: {}", diretorioRelatorios);

                Map<String, Object> parametros = new HashMap<>();
                parametros.put("SUBREPORT_DIR", diretorioRelatorios);
                parametros.put("TITULO", "Vacinas com Lotes");
                JasperPrint jasperPrint = JasperFillManager.fillReport(arquivoJasper, parametros, conexao);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } catch (JRException e) {
                logger.error("Problemas no Jasper na geracao do PDF do relatório: " + e);
            } catch (IOException e) {
                logger.error("Problemas nos arquivos de relatórios na geracao do PDF do relatório: " + e);
            }
        } catch (SQLException e) {
            logger.error("Problemas na obtenção de uma conexão com o BD na geração de relatório: " + e);
        }
        return null;
    }
}
