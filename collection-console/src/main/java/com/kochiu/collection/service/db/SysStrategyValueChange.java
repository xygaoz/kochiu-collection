package com.kochiu.collection.service.db;

import com.kochiu.collection.Constant;
import com.kochiu.collection.util.SysUtil;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.kochiu.collection.Constant.RANDOM_CHARS;

@DatabaseChange(
        name = "sysStrategyValueChange",
        description = "Insert data with values from Java methods",
        priority = ChangeMetaData.PRIORITY_DEFAULT
)
@Setter
@Slf4j
public class SysStrategyValueChange implements CustomSqlChange {

    private String tableName;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        InsertStatement statement = new InsertStatement(null, null, tableName);

        String serverUrl = Constant.ROOT_PATH;
        //  运行在docker中
        if(SysUtil.isRunningInDocker()){
            log.debug("运行在docker中");
            //判断路径是否存在
            if(Files.exists(Paths.get(Constant.CONTAINER_RESOURCE_PATH))){
                serverUrl = Constant.CONTAINER_RESOURCE_PATH;
            }
        }
        else{
            log.debug("运行在主机中");
        }

        statement.addColumnValue("strategy_id", 1);
        statement.addColumnValue("strategy_code", "local");
        statement.addColumnValue("strategy_name", "本地存储");
        statement.addColumnValue("server_url", serverUrl);
        statement.addColumnValue("CREATE_TIME", new RawParameterizedSqlStatement("datetime('now')"));
        statement.addColumnValue("CREATE_BY", "sys");
        statement.addColumnValue("DB_VERSION", 1);

        return new SqlStatement[] { statement };
    }

    @Override
    public String getConfirmationMessage() {
        return "";
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
