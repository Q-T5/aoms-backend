package npc.martin.impalabackend.apis;

import java.util.List;
import npc.martin.impalabackend.advice.QueryEmptyResult;
import npc.martin.impalabackend.entity.AnimalBasicDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import npc.martin.impalabackend.services.AnimalBasicDetailService;

/**
 *
 * @author bikathi_martin
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class AnimalBasicDetailController {
    private final AnimalBasicDetailService animalService;
    
    @Autowired
    public AnimalBasicDetailController(AnimalBasicDetailService animalService) {
        this.animalService = animalService;
    } 
   
    @GetMapping(value = "/animals/getAllAnimals")
    public ResponseEntity<List<AnimalBasicDetail>> getAnimals() {
        List<AnimalBasicDetail> animalList = animalService.getAnimals();

        if(animalList.isEmpty()) {
            throw new QueryEmptyResult("Server responded with an empty result set.");
        }

        return new ResponseEntity<>(animalList, HttpStatus.OK);
    }
    
    @PostMapping(value = "/animals/createAnimal")
    public ResponseEntity<AnimalBasicDetail> createAnimal(@RequestBody AnimalBasicDetail animal) {
        animal.setAnimalId(0);
        animalService.createAnimal(animal);

        return new ResponseEntity<>(animal, HttpStatus.CREATED);
    }
    
    @GetMapping(value = "/animals/getAnimal/{animalId}")
    public ResponseEntity<AnimalBasicDetail> getAnimalById(@PathVariable Integer animalId) {
        AnimalBasicDetail theAnimal = animalService.getAnimalById(animalId);

        if(theAnimal == null) {
            throw new QueryEmptyResult("Animal with ID: " + animalId + " not found.");
        }

        return new ResponseEntity<>(theAnimal, HttpStatus.OK);
    }
    
    @GetMapping(value = "/animal/searchAnimal/{animalId}")
    public ResponseEntity<AnimalBasicDetail> searchAnimalById(@PathVariable Integer animalId) {
        AnimalBasicDetail theAnimal = animalService.searchAnimalById(animalId);
        
        if(theAnimal == null) {
            throw new QueryEmptyResult("Animal with ID: " + animalId + " not found.");
        }

        return new ResponseEntity<>(theAnimal, HttpStatus.OK);
    }
    
    @PutMapping(value = "/animals/updateAnimal")
    public ResponseEntity<AnimalBasicDetail> updateAnimal(@RequestBody AnimalBasicDetail animal) {
        animalService.updateAnimal(animal);
        return new ResponseEntity<>(animal, HttpStatus.OK);
    }
    
    @DeleteMapping(value = "/animals/deleteAnimal/{animalId}")
    public ResponseEntity<String> deleteAnimal(@PathVariable Integer animalId) {
        AnimalBasicDetail theAnimal = animalService.getAnimalById(animalId);

        if(theAnimal == null) {
            throw new QueryEmptyResult("Animal with ID: " + animalId + " not found.");
        }
        animalService.deleteAnimal(theAnimal);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
