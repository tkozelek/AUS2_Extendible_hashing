package kozelek;

import kozelek.US.HeapFile;
import kozelek.US.Block;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        HeapFile<Car> heap = new HeapFile<>("heapfile.bin", 3, Car.class);

        for (int i = 0; i < 11; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            heap.insert(car);
        }

        ArrayList<Block<Car>> blocks = heap.getAllBlocks();

        for (Block<Car> block : blocks) {
            System.out.println(block);
        }
        System.out.println(heap);
    }
}