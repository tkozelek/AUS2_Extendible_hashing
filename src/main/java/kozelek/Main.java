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

        int blokovacifaktor = 3;

        HeapFile<Car> heap = new HeapFile<>("heapfile.bin", blokovacifaktor, Car.class);
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            cars.add(car);
            int index = heap.insert(car);
            indexes.add(index);
        }

        long sizeBefore = heap.fileSize();

        for (int i = 0; i < 10000; i++) {
            int index = rand.nextInt(cars.size());
            Car car = cars.removeLast();
            int blockIndex = indexes.removeLast();
            heap.delete(blockIndex, car);
        }

        for (int i = 0; i < 100; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            cars.add(car);
            int index = heap.insert(car);
            indexes.add(index);
        }

        long sizeAfter = heap.fileSize();

        System.out.println(heap);
        System.out.println(cars.size());
        System.out.println(heap.getRecordCount());
        System.out.format("%d %d", sizeBefore, sizeAfter);

        heap.closeFileManager();
    }
}