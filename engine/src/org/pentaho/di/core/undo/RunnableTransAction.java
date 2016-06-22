package org.pentaho.di.core.undo;

/**
 * @author nhudak
 */
public class RunnableTransAction extends TransAction {
  private static final Runnable NO_OP = new Runnable() {
    @Override public void run() {

    }
  };

  private final String name;
  private final Runnable undo;
  private final Runnable redo;

  public RunnableTransAction( String name, Runnable undo, Runnable redo ) {
    this.name = name;
    this.undo = undo != null ? undo : NO_OP;
    this.redo = redo != null ? redo : NO_OP;
  }

  public void undo() {
    undo.run();
  }

  public void redo() {
    redo.run();
  }

  @Override public String toString() {
    return name;
  }
}
