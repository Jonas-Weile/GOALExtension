package jason.asSyntax;


/**
 * Interface for elements of a plans's body.
 * 
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface PlanBody extends Term {

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
        addBelNewFocus {  @Override
		public String toString() { return "++"; }},        
        addBelBegin {     @Override
		public String toString() { return "+<"; }},       // equivalent to simple +    
        addBelEnd {       @Override
		public String toString() { return "+>"; }},      
        delBel {          @Override
		public String toString() { return "-"; }},
        delAddBel {       @Override
		public String toString() { return "-+"; }},
        achieveNF {       @Override
		public String toString() { return "!!"; }},
        constraint {      @Override
		public String toString() { return ""; }}
    }

    public BodyType    getBodyType();
    public Term        getBodyTerm();
    public PlanBody    getBodyNext();

    public boolean     isEmptyBody();
    public int         getPlanSize();

    public void setBodyType(BodyType bt);
    public void setBodyTerm(Term t);
    public void setBodyNext(PlanBody bl);
    public PlanBody getLastBody();
    
    public boolean isBodyTerm();
    public void setAsBodyTerm(boolean b);    
    
    public boolean add(PlanBody bl);
    public boolean add(int index, PlanBody bl);
    public Term removeBody(int index);  
    
    /** clone the plan body */
    public PlanBody clonePB(); 
}
