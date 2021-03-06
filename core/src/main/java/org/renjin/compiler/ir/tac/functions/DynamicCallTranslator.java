/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.codegen.FunctionLoader;
import org.renjin.compiler.codegen.NamedFunctionLoader;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.DynamicCall;
import org.renjin.compiler.ir.tac.expressions.DynamicSetterCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

public class DynamicCallTranslator extends FunctionCallTranslator {

  public static final DynamicCallTranslator INSTANCE = new DynamicCallTranslator();

  private DynamicCallTranslator() {
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, FunctionCall call) {

    if(call.getFunction() instanceof Symbol) {
      String functionName = ((Symbol) call.getFunction()).getPrintName();
      return new DynamicCall(new NamedFunctionLoader(functionName), call, functionName(call));
    } else {

      SimpleExpression functionExpr = builder.translateSimpleExpression(context, call.getFunction());
      FunctionLoader functionLoader = (c, mv) -> {
        functionExpr.getCompiledExpr(c).loadSexp(c, mv);
        mv.checkcast(Type.getType(Function.class));
      };

      return new DynamicCall(functionLoader, call, "fn");
    }
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {

    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall getterCall, Expression rhs) {
    if(getterCall.getFunction() instanceof Symbol) {
      Symbol getter = (Symbol) getterCall.getFunction();
      String setter = getter.getPrintName() + "<-";
      return new DynamicSetterCall(getterCall, new NamedFunctionLoader(setter), setter, builder.simplify(rhs));
    } else {
      throw new NotCompilableException(getterCall, "Unsupported expression in complex assignment");
    }
  }

  private String functionName(FunctionCall call) {
    if(call.getFunction() instanceof Symbol) {
      return ((Symbol) call.getFunction()).getPrintName();
    } else {
      throw new IllegalStateException();
    }
  }

}
