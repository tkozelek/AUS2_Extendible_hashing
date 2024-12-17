package kozelek;

import kozelek.US.hash.ExtendibleHashing;
import kozelek.US.hash.HashBlock;
import kozelek.US.heap.Block;
import kozelek.US.heap.HeapFile;
import kozelek.db.Database;
import kozelek.gui.controller.StartController;
import kozelek.gui.view.StartWindow;
import kozelek.objekty.Car;
import kozelek.objekty.Generator;
import kozelek.objekty.HashObjekty.CarId;
import kozelek.objekty.HashObjekty.CarSPZ;
import kozelek.objekty.Servis;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        StartWindow sw = new StartWindow();
        StartController controller = new StartController(sw);
    }
}