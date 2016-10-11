package org.apache.streams.example.test;

import org.apache.streams.mongo.test.MongoPersistIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MongoPersistIT.class,
        MongoElasticsearchSyncIT.class
})

public class SyncITs {
    // the class remains empty,
    // used only as a holder for the above annotations
}