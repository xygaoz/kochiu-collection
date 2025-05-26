package com.kochiu.collection.service.db;

import com.kochiu.collection.util.RsaHexUtil;
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

import static com.kochiu.collection.Constant.RANDOM_CHARS;

@DatabaseChange(
        name = "sysSecurityValueChange",
        description = "Insert data with values from Java methods",
        priority = ChangeMetaData.PRIORITY_DEFAULT
)
@Setter
public class SysSecurityValueChange implements CustomSqlChange {

    private String tableName;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        InsertStatement statement = new InsertStatement(null, null, tableName);

        String commonKey = RandomStringUtils.random(16, RANDOM_CHARS);
        try {
            String[] keys = RsaHexUtil.genKeyPair();

            statement.addColumnValue("ID", 1);
            statement.addColumnValue("PUBLIC_KEY", keys[0]); // 公钥
            statement.addColumnValue("PRIVATE_KEY", keys[1]); // 私钥
            statement.addColumnValue("COMMON_KEY", commonKey);
            statement.addColumnValue("CREATE_TIME", new RawParameterizedSqlStatement("datetime('now')"));
            statement.addColumnValue("CREATE_BY", "sys");
            statement.addColumnValue("DB_VERSION", 1);

            return new SqlStatement[] { statement };
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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
