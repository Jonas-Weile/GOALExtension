package jason.asSyntax;


/** 
 * @deprecated use PlanBodyImpl instead.
 * 
 * @hidden
 */
@Deprecated
public class BodyLiteral extends PlanBodyImpl {

    /** @deprecated Use BodyType of PlanBody instead */
    public enum BodyType {
        none {            @Override
		public String toString() { return ""; }},
        action {          @Override
		public String toString() { return ""; }},
        internalAction {  @Override
		public String toString() { return ""; }},
        achieve {         @Override
		public String toString() { return "!"; }},
        test {            @Override
		public String toString() { return "?"; }},
        addBel {          @Override
		public String toString() { return "+"; }},
        delBel {          @Override
		public String toString() { return "-"; }},
        delAddBel {       @Override
		public String toString() { return "-+"; }},
        achieveNF {       @Override
		public String toString() { return "!!"; }},
        constraint {      @Override
		public String toString() { return ""; }}
    }

    public BodyLiteral(BodyType t, Term b) {
        super(oldToNew(t),b);
    }
    
    private static PlanBody.BodyType oldToNew(BodyType old) {
        switch (old) {
        case action: return PlanBody.BodyType.action;
        case internalAction: return PlanBody.BodyType.internalAction;
        case achieve: return PlanBody.BodyType.achieve;
        case test: return PlanBody.BodyType.test;
        case addBel: return PlanBody.BodyType.addBel;
        case delBel: return PlanBody.BodyType.delBel;
        case delAddBel: return PlanBody.BodyType.delAddBel;
        case achieveNF: return PlanBody.BodyType.achieveNF;
        case constraint: return PlanBody.BodyType.constraint;
        default: break;
        }
        return PlanBody.BodyType.none;
    }

}
