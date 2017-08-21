/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.insert.AbstractInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.insert.InsertStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {
    
    public MySQLInsertParser(final ShardingRule shardingRule, final CommonParser commonParser) {
        super(shardingRule, commonParser);
    }
    
    @Override
    protected void parseCustomizedInsert(final InsertStatement insertStatement) {
        parseInsertSet(insertStatement);
    }
    
    private void parseInsertSet(final InsertStatement insertStatement) {
        do {
            Column column = new Column(SQLUtil.getExactlyValue(getCommonParser().getLexer().getCurrentToken().getLiterals()), insertStatement.getTables().getSingleTableName());
            getCommonParser().getLexer().nextToken();
            getCommonParser().accept(Symbol.EQ);
            SQLExpression sqlExpression;
            if (getCommonParser().equalAny(Literals.INT)) {
                sqlExpression = new SQLNumberExpression(Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getCommonParser().equalAny(Literals.FLOAT)) {
                sqlExpression = new SQLNumberExpression(Double.parseDouble(getCommonParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getCommonParser().equalAny(Literals.CHARS)) {
                sqlExpression = new SQLTextExpression(getCommonParser().getLexer().getCurrentToken().getLiterals());
            } else if (getCommonParser().equalAny(DefaultKeyword.NULL)) {
                sqlExpression = new SQLIgnoreExpression(DefaultKeyword.NULL.name());
            } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
                sqlExpression = new SQLPlaceholderExpression(insertStatement.getParametersIndex());
                insertStatement.increaseParametersIndex();
            } else {
                throw new UnsupportedOperationException("");
            }
            getCommonParser().getLexer().nextToken();
            if (getCommonParser().equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                insertStatement.getConditions().add(new Condition(column, sqlExpression), getShardingRule());
            } else {
                getCommonParser().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getCommonParser().skipIfEqual(Symbol.COMMA));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[] {MySQLKeyword.PARTITION};
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForValues() {
        return new Keyword[] {MySQLKeyword.VALUE};
    }
    
    @Override
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[] {DefaultKeyword.SET};
    }
}
