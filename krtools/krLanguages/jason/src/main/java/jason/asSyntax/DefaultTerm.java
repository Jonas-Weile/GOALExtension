// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for all terms.
 * 
 * (this class may be renamed to AbstractTerm in future releases of Jason, so
 * avoid using it -- use ASSyntax class to create new terms)
 * 
 * @navassoc - source - SourceInfo
 * @opt nodefillcolor lightgoldenrodyellow
 * 
 * @see ASSyntax
 */
public abstract class DefaultTerm implements Term, Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Term.class.getName());

    protected Integer    hashCodeCache = null;
    protected SourceInfo srcInfo       = null;

    /** @deprecated it is preferable to use ASSyntax.parseTerm */
    @Deprecated
	public static Term parse(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            return parser.term();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing term " + sTerm,e);
            return null;
        }
    }
            
    @Override
	public boolean isVar()            { return false; }
    @Override
	public boolean isUnnamedVar()     { return false; }
    @Override
	public boolean isLiteral()        { return false; }
    @Override
	public boolean isRule()           { return false; }
    @Override
	public boolean isList()           { return false; }
    @Override
	public boolean isString()         { return false; }
    @Override
	public boolean isInternalAction() { return false; }
    @Override
	public boolean isArithExpr()      { return false; }
    @Override
	public boolean isNumeric()        { return false; }
    @Override
	public boolean isPred()           { return false; }
    @Override
	public boolean isStructure()      { return false; }
    @Override
	public boolean isAtom()           { return false; }
    @Override
	public boolean isPlanBody()       { return false; }
    @Override
	public boolean isGround()         { return true; }
    @Override
	public boolean isCyclicTerm()     { return false; }
    @Override
	public VarTerm getCyclicVar()     { return null; }

    @Override
	public boolean hasVar(VarTerm t, Unifier u)    { return false; }
    
    @Override
	public void countVars(Map<VarTerm, Integer> c) {}
    
    @Override
	abstract public    Term   clone();
    abstract protected int    calcHashCode();
    
    @Override
	public int hashCode() {
        if (hashCodeCache == null) 
            hashCodeCache = calcHashCode();
        return hashCodeCache;
    }

    public void resetHashCodeCache() {
        hashCodeCache = null;
    }
    
    @Override
	public int compareTo(Term t) {
        if (t == null) 
            return -1;
        else
            return this.toString().compareTo(t.toString());
    }

    @Override
	public boolean subsumes(Term l) { 
        if (l.isVar())
            return false;
        else
            return true; 
    }

    @Override
	public Term capply(Unifier u) {
        return clone();
    }
    
    @Override
	public SourceInfo getSrcInfo() {
        return srcInfo;
    }

    @Override
	public void setSrcInfo(SourceInfo s) {
        srcInfo = s;
    }
    
    public String getErrorMsg() {
        if (srcInfo == null)
            return "";
        else 
            return srcInfo.toString();
    }

}
