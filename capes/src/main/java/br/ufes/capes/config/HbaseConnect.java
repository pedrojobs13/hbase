package br.ufes.capes.config;

import br.ufes.capes.entity.dao.HbaseClientOperations;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class HbaseConnect {

    private final HbaseClientOperations HBaseClientOperations;

    @PostConstruct
    private void connect() throws IOException {
        Configuration config = HBaseConfiguration.create();

        String path = this.getClass()
                .getClassLoader()
                .getResource("hbase-site.xml")
                .getPath();
        config.addResource(new Path(path));

        try {
            HBaseAdmin.available(config);
            HBaseClientOperations.run(config);
        } catch (MasterNotRunningException e) {
            log.error("HBase is not running: " + e.getMessage(), e);
            System.exit(1);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage(), e);
            throw e;
        }
    }
}