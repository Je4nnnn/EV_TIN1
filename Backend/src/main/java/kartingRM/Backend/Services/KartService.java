package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.KartEntity;
import kartingRM.Backend.Repositories.KartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KartService {

    @Autowired
    private KartRepository kartRepository;

    public List<KartEntity> getAllKarts() {
        return kartRepository.findAll();
    }

    public KartEntity getKartById(long id) { return kartRepository.findById(id).get(); }

    public KartEntity saveKart(KartEntity kart) {
        return kartRepository.save(kart);
    }
}