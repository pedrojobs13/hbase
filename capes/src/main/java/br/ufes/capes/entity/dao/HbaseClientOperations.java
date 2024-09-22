package br.ufes.capes.entity.dao;

import br.ufes.capes.entity.Projeto;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class HbaseClientOperations {

    private Connection connection;

    public void run(Configuration config) throws IOException {
        this.connection = ConnectionFactory.createConnection(config);
    }

    public void createTable() throws IOException {
        try (Admin admin = connection.getAdmin()) {
            TableDescriptor tableDescriptor = TableDescriptorBuilder
                    .newBuilder(TableName.valueOf("capes"))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("artigos".getBytes()).build())
                    .build();

            if (!admin.tableExists(tableDescriptor.getTableName())) {
                admin.createTable(tableDescriptor);
                log.info("Table created");
            } else {
                log.info("Table already exists");
            }
        } catch (IOException e) {
            log.error("Error creating table: " + e.getMessage(), e);
            throw e;
        }
    }

    public void insertData(String rowKey, String family, String qualifier, String value) throws IOException {
        Table table = connection.getTable(TableName.valueOf("capes"));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
        table.put(put);
        table.close();
    }

    public String getData(String rowKey, String family, String qualifier) throws IOException {
        Table table = connection.getTable(TableName.valueOf("capes"));
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        Result result = table.get(get);
        byte[] value = result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        table.close();
        return value != null ? Bytes.toString(value) : null;
    }
    public boolean dataExists(String rowKey, String family, String qualifier) throws IOException {
        Table table = connection.getTable(TableName.valueOf("capes"));
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        boolean exists = table.exists(get);
        table.close();
        return exists;
    }
    public void updateData(String rowKey, String family, String qualifier, String value) throws IOException {
        insertData(rowKey, family, qualifier, value);
    }

    public void deleteData(String rowKey, String family, String qualifier) throws IOException {
        Table table = connection.getTable(TableName.valueOf("capes"));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        table.delete(delete);
        table.close();
    }
    public List<Projeto> listAllData() throws IOException {
        List<Projeto> projetos = new ArrayList<>();
        Table table = connection.getTable(TableName.valueOf("capes"));
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            String rowKey = Bytes.toString(result.getRow());
            String autor = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("autor")));
            String resumo = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("resumo")));
            String url = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("url")));
            String ano = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("ano")));
            String publicacao = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("publicacao")));
            String tipoDoRecurso = Bytes.toString(result.getValue(Bytes.toBytes("artigos"), Bytes.toBytes("tipoDoRecurso")));

            Projeto projeto = Projeto.builder()
                    .title(rowKey)
                    .autor(autor)
                    .resumo(resumo)
                    .url(url)
                    .ano(ano)
                    .publicacao(publicacao)
                    .tipoDoRecurso(tipoDoRecurso)
                    .build();
            projetos.add(projeto);
        }
        scanner.close();
        table.close();
        return projetos;
    }

}