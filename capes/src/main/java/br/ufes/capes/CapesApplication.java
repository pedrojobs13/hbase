package br.ufes.capes;

import br.ufes.capes.entity.dao.HbaseClientOperations;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

@SpringBootApplication
public class CapesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapesApplication.class, args);
	}

	private void connect() throws IOException {
		Configuration config = HBaseConfiguration.create();

		String path = this.getClass().getClassLoader().getResource("hbase-site.xml").getPath();

		config.addResource(new Path(path));

		try {
			HBaseAdmin.available(config);
		} catch (MasterNotRunningException e) {
			System.out.println("HBase is not running." + e.getMessage());
			return;
		}
//
//		HbaseClientOperations HBaseClientOperations = new HBaseClientOperations();
//		HBaseClientOperations.r(config);
	}

}
