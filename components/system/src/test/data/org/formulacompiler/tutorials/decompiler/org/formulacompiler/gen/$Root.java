package org.formulacompiler.gen;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v1;
import org.formulacompiler.tutorials.Decompilation;

final class $Root implements Computation, Decompilation.MyOutputs
{
    private final Decompilation.MyInputs $inputs;
    final Environment $environment;
    
    $Root(Decompilation.MyInputs myinputs, Environment environment) {
        $environment = environment;
        $inputs = myinputs;
    }
    
    final double get$0() {
        return RuntimeDouble_v1.max(get$1(), get$2());
    }
    
    public final double rebateOp() {
        return get$0();
    }
    
    final double get$1() {
        return $inputs.customerRebate();
    }
    
    final double get$2() {
        return $inputs.articleRebate();
    }
}