/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.core.measure;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sonar.api.measures.Metric;

public class MeasureFilterCondition {
  public enum Operator {
    EQUALS("eq", "="), GREATER("gt", ">"), GREATER_OR_EQUALS("gte", ">="), LESS("lt", "<"), LESS_OR_EQUALS("lte", "<=");

    private String code;
    private String sql;

    private Operator(String code, String sql) {
      this.code = code;
      this.sql = sql;
    }

    public String getSql() {
      return sql;
    }

    public static Operator fromCode(String code) {
      for (Operator operator : values()) {
        if (operator.code.equals(code)) {
          return operator;
        }
      }
      throw new IllegalArgumentException("Unknown operator code: " + code);
    }
  }

  private final Metric metric;
  private final Operator operator;
  private final double value;
  private Integer period = null;

  public MeasureFilterCondition(Metric metric, Operator operator, double value) {
    this.metric = metric;
    this.operator = operator;
    this.value = value;
  }

  public MeasureFilterCondition setPeriod(Integer period) {
    this.period = period;
    return this;
  }

  public Metric metric() {
    return metric;
  }

  public Operator operator() {
    return operator;
  }

  public double value() {
    return value;
  }

  public Integer period() {
    return period;
  }

  StringBuilder appendSqlColumn(StringBuilder sb, int conditionIndex) {
    sb.append("pmcond").append(conditionIndex);
    if (period != null) {
      sb.append(".variation_value_").append(period).toString();
    } else {
      sb.append(".value");
    }
    return sb;
  }

  StringBuilder appendSqlCondition(StringBuilder sql, int conditionIndex) {
    String table = "pmcond" + conditionIndex;
    sql.append(" ").append(table).append(".metric_id=");
    sql.append(metric.getId());
    sql.append(" AND ");
    appendSqlColumn(sql, conditionIndex);
    sql.append(operator.getSql()).append(value);
    sql.append(" AND ");
    sql.append(table).append(".rule_id IS NULL AND ");
    sql.append(table).append(".rule_priority IS NULL AND ");
    sql.append(table).append(".characteristic_id IS NULL AND ");
    sql.append(table).append(".person_id IS NULL ");
    return sql;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SIMPLE_STYLE);
  }
}
