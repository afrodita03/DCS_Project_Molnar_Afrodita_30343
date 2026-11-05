
package UVS_Fuzzy;

import java.util.ArrayList;
import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.FV;
import DataOnly.Fuzzy;
import DataOnly.PlaceNameWithWeight;
import DataOnly.TransferOperation;
import Enumerations.FZ;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class Intersection {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Fuzzy Intersection";
        pn.NetworkPort = 1080;

        for (int i = 1; i <= 4; i++) {
            addPlace(pn, "P_i" + i);
        }

        addPlace(pn, "P_12");
        addPlace(pn, "P_34");
        addPlace(pn, "P_i");

        for (int i = 1; i <= 4; i++) {
            addPlace(pn, "P_o" + i);
        }

        addConstant(pn, "C_025", 0.25F);
        addConstant(pn, "C_050", 0.50F);
        addConstant(pn, "C_012", 0.12F);

        for (int i = 1; i <= 4; i++) {
            DataTransfer output = new DataTransfer();
            output.SetName("P_out" + i);
            output.Value = new TransferOperation(
                    "localhost",
                    String.valueOf(1084 + i),
                    "P_i"
            );
            pn.PlaceList.add(output);
        }

        FLRS plusRules = new FLRS(
                new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
                new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM),
                new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
                new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL),
                new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL), new FV(FZ.PL)
        );

        addTwoInputFLRS(
                pn, "t1", "P_i1", "P_i2", "P_12", plusRules
        );

        addTwoInputFLRS(
                pn, "t2", "P_i3", "P_i4", "P_34", plusRules
        );

        addTwoInputFLRS(
                pn, "t3", "P_12", "P_34", "P_i", plusRules
        );

        PetriTransition split = new PetriTransition(pn);
        split.TransitionName = "t_s";
        split.InputPlaceName.add("P_i");

        GuardMapping splitGuard = new GuardMapping();
        splitGuard.condition = new Condition(
                split, "P_i", TransitionCondition.NotNull
        );

        String[] constantNames = {
                "C_025", "C_050", "C_012", "C_012"
        };

        for (int i = 1; i <= 4; i++) {
            ArrayList<PlaceNameWithWeight> inputs = new ArrayList<>();

            inputs.add(new PlaceNameWithWeight("P_i", 1F));
            inputs.add(new PlaceNameWithWeight(
                    constantNames[i - 1], 1F
            ));

            splitGuard.Activations.add(new Activation(
                    inputs,
                    TransitionOperation.Prod_Fuzzy,
                    "P_o" + i,
                    split
            ));
        }

        split.GuardMappingList.add(splitGuard);
        split.Delay = 0;
        pn.Transitions.add(split);

        for (int i = 1; i <= 4; i++) {
            PetriTransition exit = new PetriTransition(pn);
            exit.TransitionName = "t_out" + i;
            exit.InputPlaceName.add("P_o" + i);

            GuardMapping exitGuard = new GuardMapping();
            exitGuard.condition = new Condition(
                    exit, "P_o" + i, TransitionCondition.NotNull
            );

            exitGuard.Activations.add(new Activation(
                    exit,
                    "P_o" + i,
                    TransitionOperation.SendOverNetwork,
                    "P_out" + i
            ));

            exit.GuardMappingList.add(exitGuard);
            exit.Delay = 5;
            pn.Transitions.add(exit);
        }

        pn.Delay = 1000;

        System.out.println("Intersection ready on port 1080");

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }

    private static void addPlace(PetriNet pn, String name) {
        DataFuzzy place = new DataFuzzy();
        place.SetName(name);
        pn.PlaceList.add(place);
    }

    private static void addConstant(PetriNet pn, String name, float value) {
        DataFuzzy constant = new DataFuzzy();
        constant.SetName(name);
        constant.SetValue(new Fuzzy(value));
        pn.ConstantPlaceList.add(constant);
    }

    private static void addTwoInputFLRS(
            PetriNet pn,
            String transitionName,
            String first,
            String second,
            String destination,
            FLRS rules) {

        PetriTransition transition = new PetriTransition(pn);
        transition.TransitionName = transitionName;
        transition.InputPlaceName.add(first);
        transition.InputPlaceName.add(second);

        Condition c1 = new Condition(
                transition, first, TransitionCondition.NotNull
        );

        Condition c2 = new Condition(
                transition, second, TransitionCondition.NotNull
        );

        c1.SetNextCondition(LogicConnector.AND, c2);

        GuardMapping guard = new GuardMapping();
        guard.condition = c1;

        ArrayList<PlaceNameWithWeight> inputs = new ArrayList<>();
        inputs.add(new PlaceNameWithWeight(first, 1F));
        inputs.add(new PlaceNameWithWeight(second, 1F));

        ArrayList<String> outputs = new ArrayList<>();
        outputs.add(destination);

        guard.Activations.add(new Activation(
                transition,
                rules,
                inputs,
                TransitionOperation.FLRS,
                outputs
        ));

        transition.GuardMappingList.add(guard);
        transition.Delay = 0;
        pn.Transitions.add(transition);
    }
}
