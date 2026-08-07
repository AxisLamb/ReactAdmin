package com.lain.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 兼容 PostgreSQL TIMESTAMPTZ 的 LocalDateTime 类型处理器
 * <p>
 * PG JDBC 驱动不允许将 TIMESTAMPTZ 列直接转换为 LocalDateTime，
 * 默认的 LocalDateTimeTypeHandler 会抛出 PSQLException。
 * 此处理器先取出原始值再做类型转换，
 * 同时兼容 timestamp、timestamptz 两种列类型，
 * 适用于 MySQL / PostgreSQL / SQL Server / Oracle 多数据库切换。
 */
@MappedTypes(LocalDateTime.class)
public class PostgresLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getObject(columnName));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getObject(columnIndex));
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getObject(columnIndex));
    }

    /**
     * 将数据库返回的原始值转换为 LocalDateTime
     */
    private LocalDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof OffsetDateTime) {
            // timestamptz 列可能返回 OffsetDateTime，直接取本地时间部分
            return ((OffsetDateTime) value).toLocalDateTime();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        // 兜底：按 ISO 格式字符串解析
        return LocalDateTime.parse(value.toString());
    }
}
