package kozelek.db;

import kozelek.US.hash.ExtendibleHashing;
import kozelek.US.hash.HashBlock;
import kozelek.US.heap.Block;
import kozelek.US.heap.HeapFile;
import kozelek.objekty.Car;
import kozelek.objekty.HashObjekty.CarId;
import kozelek.objekty.HashObjekty.CarSPZ;
import kozelek.objekty.Servis;

import java.time.LocalDate;
import java.util.ArrayList;

public class Database {
    private ExtendibleHashing<CarId> extId;
    private ExtendibleHashing<CarSPZ> extSPZ;
    private HeapFile<Car> heap;

    public Database(int maxRecordCount, String fileName) {
        extId = new ExtendibleHashing<>("id_" + fileName, CarId.class, maxRecordCount);
        extSPZ = new ExtendibleHashing<>("spz_" + fileName, CarSPZ.class, maxRecordCount);
        heap = new HeapFile<>("heap_" + fileName, Car.class, maxRecordCount);
    }

    public Database(String fileName, int maxId, int maxSPZ, int maxHeap) {
        extId = new ExtendibleHashing<>(fileName, CarId.class, maxId);
        extSPZ = new ExtendibleHashing<>(fileName, CarSPZ.class, maxSPZ);
        heap = new HeapFile<>(fileName, Car.class, maxHeap);
    }

    public int findIndexById(int id) {
        CarId cid = extId.getRecord(new CarId(id, 0));
        if (cid == null) return -1;
        return cid.getAddress();
    }

    public int findIndexBySPZ(String spz) {
        CarSPZ cspz = extSPZ.getRecord(new CarSPZ(spz, 0));
        if (cspz == null) return -1;
        return cspz.getAddress();
    }

    public Car findCarById(int id) {
        int index = findIndexById(id);
        if (index == -1) return null;
        return heap.getRecord(index, new Car(id));
    }

    public Car findCarBySPZ(String SPZ) {
        int index = findIndexBySPZ(SPZ);
        if (index == -1) return null;
        return heap.getRecord(index, new Car(SPZ));
    }

    public void addServiceVisitById(int id, Servis servis) {
        int index = findIndexById(id);
        if (index == -1) return;

        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(id));
        if (car == null) {
            throw new RuntimeException("nenajdene auto: " + id);
        }
        car.addServis(servis);
        heap.update(index, block);
    }

    public Car addServiceVisitById(int id, double cena, LocalDate datum, ArrayList<String> popis) {
        int index = findIndexById(id);
        if (index == -1) return null;
        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(id));
        if (car == null) {
            throw new RuntimeException("nenajdene auto: " + id);
        }
        Servis serv = new Servis(datum, cena);
        for (String p : popis) {
            serv.addPopis(p);
        }
        car.addServis(serv);
        heap.update(index, block);
        return car;
    }

    public void addServiceVisitBySPZ(String SPZ, double cena, LocalDate datum, String[] popis) {
        int index = findIndexBySPZ(SPZ);
        if (index == -1) return;
        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(SPZ));
        if (car == null) {
            throw new RuntimeException("nenajdene auto: " + SPZ);
        }
        Servis serv = new Servis(datum, cena);
        for (String p : popis) {
            serv.addPopis(p);
        }
        car.addServis(serv);
        heap.update(index, block);
    }

    public void addServiceVisitBySPZ(String SPZ, Servis servis) {
        int index = findIndexBySPZ(SPZ);
        if (index == -1) return;
        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(SPZ));
        if (car == null) {
            throw new RuntimeException("nenajdene auto: " + SPZ);
        }
        car.addServis(servis);
        heap.update(index, block);
    }

    public void editCar(Car oldCar, Car newCar) {
        int index = 0;
        if (oldCar.getId() != -1) {
            index = findIndexById(oldCar.getId());
        } else if (oldCar.getSPZ() != null) {
            index = findIndexBySPZ(oldCar.getSPZ());
        }
        heap.edit(index, oldCar, newCar);
        extId.edit(new CarId(oldCar.getId(), index), new CarId(newCar.getId(), index));
        extSPZ.edit(new CarSPZ(oldCar.getSPZ(), index), new CarSPZ(newCar.getSPZ(), index));
    }

    public void deleteServiceVisitById(int id, Servis servis) {
        int index = findIndexById(id);
        if (index == -1) return;
        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(id));

        if (car == null) {
            throw new RuntimeException("Auto nanjdene: " + id);
        }
        car.removeServis(servis);
        heap.update(index, block);
    }

    public void deleteServiceVisitBySPZ(String SPZ, Servis servis) {
        int index = findIndexBySPZ(SPZ);
        if (index == -1) return;
        Block<Car> block = heap.getBlock(index);
        Car car = block.getRecord(new Car(SPZ));

        if (car == null) {
            throw new RuntimeException("Auto nanjdene: " + SPZ);
        }
        car.removeServis(servis);
        heap.update(index, block);
    }

    public void deleteCarById(int id) {
        int index = findIndexById(id);
        if (index == -1) return;
        Car car = heap.getRecord(index, new Car(id));
        if (car == null) {
            throw new RuntimeException("Car not found with ID: " + id);
        }

        extId.delete(new CarId(car.getId(), 0));
        extSPZ.delete(new CarSPZ(car.getSPZ(), 0));
        heap.delete(index, car);
    }

    public void deleteCarBySPZ(String SPZ) {
        int index = findIndexBySPZ(SPZ);
        if (index == -1) return;
        Car car = heap.getRecord(index, new Car(SPZ));
        if (car == null) {
            throw new RuntimeException("Car not found with SPZ: " + SPZ);
        }
        extId.delete(new CarId(car.getId(), 0));
        extSPZ.delete(new CarSPZ(car.getSPZ(), 0));
        heap.delete(index, car);
    }

    public ArrayList<Car> getAllCars() {
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Block<Car>> bCars = heap.getAllBlocks();
        for (Block<Car> block : bCars) {
            cars.addAll(block.getRecords());
        }
        return cars;
    }

    public ArrayList<CarId> getAllCarsId() {
        ArrayList<CarId> cars = new ArrayList<>();
        ArrayList<HashBlock<CarId>> cids = extId.getAllHashBlocks();
        for (HashBlock<CarId> block : cids) {
            cars.addAll(block.getRecords());
        }
        return cars;
    }

    public ArrayList<CarSPZ> getAllCarsSPZ() {
        ArrayList<CarSPZ> cars = new ArrayList<>();
        ArrayList<HashBlock<CarSPZ>> cids = extSPZ.getAllHashBlocks();
        for (HashBlock<CarSPZ> block : cids) {
            cars.addAll(block.getRecords());
        }
        return cars;
    }

    public void insert(Car car) {
        if (findIndexById(car.getId()) != -1) {
            throw new RuntimeException("Auto s idckom:" + car.getId() + " existuje");
        }
        int address = heap.insert(car);
        extId.insert(new CarId(car.getId(), address));
        extSPZ.insert(new CarSPZ(car.getSPZ(), address));
    }

    public void insert(String meno, String priezvisko, int id, String spz) {
        this.insert(new Car(meno, priezvisko, id, spz));
    }

    public void saveStates() {
        heap.saveState();
        extId.saveState();
        extSPZ.saveState();
    }

    public void close() {
        heap.closeFileManager();
        extId.closeFileManager();
        extSPZ.closeFileManager();
    }

    public void zmazSubory() {
        heap.getFileManager().getFile().delete();
        extId.getFileManager().getFile().delete();
        extSPZ.getFileManager().getFile().delete();
    }
}
