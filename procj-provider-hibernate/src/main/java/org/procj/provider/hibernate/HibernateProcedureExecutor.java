package org.procj.provider.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.procedure.ProcedureCall;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

public class HibernateProcedureExecutor implements ProcedureExecutor {

  private final Session session;

  public HibernateProcedureExecutor(Session session) {
    this.session = session;
  }

  @Override
  public Procedure getProcedure(String signature) {
    final ProcedureCall procedure = session.createStoredProcedureCall(signature);
    return new HibernateProcedure(procedure, session);
  }

  @Override
  public void shutdown() {
    final Transaction tx = session.getTransaction();
    if (tx.isActive()) {
      tx.rollback();
    }
    session.close();
  }
}
