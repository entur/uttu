package no.entur.uttu.model;

public interface LineVisitor {
  void visitFixedLine(FixedLine fixedLine);
  void visitFlexibleLine(FlexibleLine flexibleLine);
}
