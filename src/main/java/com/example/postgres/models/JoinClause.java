package com.example.postgres.models;

class JoinClause {
    private String joinType;
    private String table;
    private String onCondition;

    public JoinClause(String joinType, String table, String onCondition) {
        this.joinType = joinType;
        this.table = table;
        this.onCondition = onCondition;
    }

    public String toSQL() {
        return joinType + " JOIN " + table + " ON " + onCondition;
    }
}
