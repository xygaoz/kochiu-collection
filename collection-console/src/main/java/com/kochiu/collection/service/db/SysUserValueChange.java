package com.kochiu.collection.service.db;

import com.kochiu.collection.util.RsaHexUtil;
import com.kochiu.collection.util.SHA256Util;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import static com.kochiu.collection.Constant.RANDOM_CHARS;

@DatabaseChange(
        name = "sysUserValueChange",
        description = "Insert data with values from Java methods",
        priority = ChangeMetaData.PRIORITY_DEFAULT
)
@Setter
public class SysUserValueChange implements CustomSqlChange {

    private String tableName;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        InsertStatement statement = new InsertStatement(null, null, tableName);

        String userCode = StringUtils.isNotBlank(System.getenv("username")) ? System.getenv("username") : "admin";
        String password = StringUtils.isNotBlank(System.getenv("password")) ? System.getenv("password") : "admin";
        password = SHA256Util.encryptBySHA256(password);

        statement.addColumnValue("USER_ID", 1);
        statement.addColumnValue("USER_CODE", userCode);
        statement.addColumnValue("USER_NAME", "超级管理员");
        statement.addColumnValue("PASSWORD", password);
        statement.addColumnValue("status", 1);
        statement.addColumnValue("can_del", 0);
        statement.addColumnValue("strategy", "local");
        statement.addColumnValue("key", RandomStringUtils.random(12, RANDOM_CHARS));
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
