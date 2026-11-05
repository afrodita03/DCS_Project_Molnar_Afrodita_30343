
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

public class Lane4 {
    public static void main(String[] args) {

        FLRS moveRules = new FLRS(
                new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
                new FV(FZ.PM), new FV(FZ.PL));

        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Fuzzy Lane 4";
        pn.NetworkPort = 1084;

        DataFuzzy input = new DataFuzzy();
        input.SetName("P_i");
        pn.PlaceList.add(input);

        DataFuzzy lane_start = new DataFuzzy();
        lane_start.SetName("P_l");
        pn.PlaceList.add(lane_start);

        DataFuzzy lane_end = new DataFuzzy();
        lane_end.SetName("lane");
        pn.PlaceList.add(lane_end);

        DataFuzzy trafficLight = new DataFuzzy();
        trafficLight.SetName("P_TL");
        trafficLight.SetValue(new Fuzzy(0F));
        pn.ConstantPlaceList.add(trafficLight);

        DataFuzzy green = new DataFuzzy();
        green.SetName("green");
        green.SetValue(new Fuzzy(0F));
        pn.ConstantPlaceList.add(green);

        DataTransfer output = new DataTransfer();
        output.SetName("P_out");
        output.Value = new TransferOperation("localhost", "1080", "P_i4");
        pn.PlaceList.add(output);

        PetriTransition tIn = new PetriTransition(pn);
        tIn.TransitionName = "t_in";
        tIn.InputPlaceName.add("P_i");

        GuardMapping inGuard = new GuardMapping();
        inGuard.condition = new Condition(
                tIn, "P_i", TransitionCondition.NotNull);

        ArrayList<PlaceNameWithWeight> inWeights = new ArrayList<>();
        inWeights.add(new PlaceNameWithWeight("P_i", 1F));

        ArrayList<String> inOutputs = new ArrayList<>();
        inOutputs.add("P_l");

        inGuard.Activations.add(new Activation(
                tIn, moveRules, inWeights,
                TransitionOperation.FLRS, inOutputs));

        tIn.GuardMappingList.add(inGuard);
        tIn.Delay = 0;
        pn.Transitions.add(tIn);

        PetriTransition tSend = new PetriTransition(pn);
        tSend.TransitionName = "t_send";
        tSend.InputPlaceName.add("P_l");

        GuardMapping sendGuard = new GuardMapping();
        sendGuard.condition = new Condition(
                tSend, "P_l", TransitionCondition.NotNull);

        ArrayList<PlaceNameWithWeight> sendWeights = new ArrayList<>();
        sendWeights.add(new PlaceNameWithWeight("P_l", 1F));

        ArrayList<String> sendOutputs = new ArrayList<>();
        sendOutputs.add("lane");

        sendGuard.Activations.add(new Activation(
                tSend, moveRules, sendWeights,
                TransitionOperation.FLRS, sendOutputs));

        tSend.GuardMappingList.add(sendGuard);
        tSend.Delay = 0;
        pn.Transitions.add(tSend);

        PetriTransition tOut = new PetriTransition(pn);
        tOut.TransitionName = "t_out";
        tOut.InputPlaceName.add("lane");
        tOut.InputPlaceName.add("P_TL");

        Condition hasTraffic = new Condition(
                tOut, "lane", TransitionCondition.NotNull);

        Condition isGreen = new Condition(
                tOut, "P_TL", TransitionCondition.Equal, "green");

        hasTraffic.SetNextCondition(LogicConnector.AND, isGreen);

        GuardMapping outGuard = new GuardMapping();
        outGuard.condition = hasTraffic;

        outGuard.Activations.add(new Activation(
                tOut, "lane",
                TransitionOperation.SendOverNetwork, "P_out"));

        outGuard.Activations.add(new Activation(
                tOut, "P_TL",
                TransitionOperation.Move, "P_TL"));

        tOut.GuardMappingList.add(outGuard);
        tOut.Delay = 5;
        pn.Transitions.add(tOut);

        System.out.println(
                "Fuzzy Lane 4 ready; destination: intersection P_i4 on port 1080");

        pn.Delay = 1000;

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }
}
