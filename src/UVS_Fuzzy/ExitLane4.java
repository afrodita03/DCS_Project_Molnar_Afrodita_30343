
package UVS_Fuzzy;

import java.util.ArrayList;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataOnly.FLRS;
import DataOnly.FV;
import DataOnly.PlaceNameWithWeight;
import Enumerations.FZ;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class ExitLane4 {
    public static void main(String[] args) {

        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Fuzzy Exit Lane 4";
        pn.NetworkPort = 1088;

        DataFuzzy input = new DataFuzzy();
        input.SetName("P_i");
        pn.PlaceList.add(input);

        DataFuzzy output = new DataFuzzy();
        output.SetName("P_exit");
        pn.PlaceList.add(output);

        FLRS moveRules = new FLRS(
                new FV(FZ.NL),
                new FV(FZ.NM),
                new FV(FZ.ZR),
                new FV(FZ.PM),
                new FV(FZ.PL)
        );

        PetriTransition t1 = new PetriTransition(pn);
        t1.TransitionName = "t_1";
        t1.InputPlaceName.add("P_i");

        GuardMapping guard = new GuardMapping();
        guard.condition = new Condition(
                t1, "P_i", TransitionCondition.NotNull
        );

        ArrayList<PlaceNameWithWeight> inputs = new ArrayList<>();
        inputs.add(new PlaceNameWithWeight("P_i", 1F));

        ArrayList<String> outputs = new ArrayList<>();
        outputs.add("P_exit");

        guard.Activations.add(new Activation(
                t1,
                moveRules,
                inputs,
                TransitionOperation.FLRS,
                outputs
        ));

        t1.GuardMappingList.add(guard);
        t1.Delay = 0;
        pn.Transitions.add(t1);

        pn.Delay = 1000;

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }
}
