package org.procj.provider.hibernate;

import javax.persistence.ParameterMode;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.result.ResultSetOutput;
import org.procj.provider.spi.Procedure;

class HibernateProcedure implements Procedure {

  private final ProcedureCall procedure;
  private Object result;

  HibernateProcedure(ProcedureCall procedure) {
    this.procedure = procedure;
  }

  @Override
  public void setParameterIn(int index, Object value) {
    procedure.registerParameter(index, value.getClass(), ParameterMode.IN);
    procedure.setParameter(index, value);
  }

  @Override
  public Object getReturnValue() {
    return result;
  }

  @Override
  public void execute() {
    try {
      procedure.execute();
      if (procedure.getOutputs().getCurrent() instanceof ResultSetOutput) {
        result = procedure.getResultList();
      }
    } finally {
      procedure.unwrap(ProcedureOutputs.class).release();
    }
  }
}
