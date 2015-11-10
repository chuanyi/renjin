package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ProvidedRecordTest extends AbstractGccTest {
  
  @Test
  public void test() throws Exception {
    
    GimpleCompilationUnit unit = compileToGimple("provided_records.c");

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.addRecordClass("jvm_rect", JvmRect.class);
    compiler.addMethod("jvm_area", JvmRect.class, "area");
    compiler.compile(Collections.singletonList(unit));

    Class<?> clazz = Class.forName("org.renjin.gcc.provided_records");
    Method testMethod = clazz.getMethod("test", JvmRect.class);
  
    double area = (Integer)testMethod.invoke(null, new JvmRect(20, 3));
  
    assertThat(area, equalTo(60d));
  
  }
  
  
}
