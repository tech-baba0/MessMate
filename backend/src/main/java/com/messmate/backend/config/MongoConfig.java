package com.messmate.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "messmate";
    }

    @Override
    public MongoClient mongoClient() {
        // Enforcing strict MongoClient connection to bypass any Render environment
        // variable typos or YAML binding bugs
        return MongoClients.create(
                "mongodb+srv://techbaba0026_db_user:DJF6GvkGcXUNO43j@messmatedata.exdhldv.mongodb.net/messmate?appName=MessMateData");
    }
}
