package kozelek;

import kozelek.US.HeapFile;
import kozelek.US.Block;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random rand = new Random();

        rand.setSeed(1);
        int blokovacifaktor = 3;

        HeapFile<Car> heap = new HeapFile<>("heapfile.bin", blokovacifaktor, Car.class);
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            cars.add(car);
            int index = heap.insert(car);
            indexes.add(index);
        }

        heap.printAll();
        System.out.println(heap);



        System.out.println("_________________________");

        for (int i = 0; i < 100; i++) {
            int index = rand.nextInt(indexes.size());
            Car car = cars.remove(index);
            int indexBlock = indexes.remove(index);

            heap.delete(indexBlock, car);
        }

        for (int i = 0; i < 200; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            cars.add(car);
            int index = heap.insert(car);
            indexes.add(index);
        }

        for (int i = 0; i < 100; i++) {
            int index = rand.nextInt(indexes.size());
            Car car = cars.remove(index);
            int indexBlock = indexes.remove(index);

            heap.delete(indexBlock, car);
        }

        heap.printAll();
        System.out.println(heap);

        heap.closeFileManager();
    }
}