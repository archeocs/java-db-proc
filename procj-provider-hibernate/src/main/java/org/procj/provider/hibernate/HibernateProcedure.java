package org.procj.provider.hibernate;

import javax.persistence.ParameterMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.result.ResultSetOutput;
import org.procj.provider.spi.Procedure;

class HibernateProcedure implements Procedure {

  private final ProcedureCall procedure;
  private Object result;
  private final Session session;

  HibernateProcedure(ProcedureCall procedure, Session session) {
    this.procedure = procedure;
    this.session = session;
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
      final Transaction tx = session.getTransaction();
      tx.begin();
      procedure.execute();
      if (procedure.getOutputs().getCurrent() instanceof ResultSetOutput) {
        result = procedure.getResultList();
      } else {
        procedure.getUpdateCount();
      }
      tx.commit();
    } finally {
      procedure.unwrap(ProcedureOutputs.class).release();
    }
  }
}
