package ro.musiclover.manicureappointments.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.musiclover.manicureappointments.entity.NailsService;
import ro.musiclover.manicureappointments.exception.BusinessException;
import ro.musiclover.manicureappointments.mapper.NailsServiceMapper;
import ro.musiclover.manicureappointments.model.nails_services.NailsServiceRequest;
import ro.musiclover.manicureappointments.model.nails_services.RequestUpdateName;
import ro.musiclover.manicureappointments.model.nails_services.NailsServiceResponse;
import ro.musiclover.manicureappointments.model.nails_services.RequestUpdatePrice;
import ro.musiclover.manicureappointments.repository.MyRepository;
import ro.musiclover.manicureappointments.repository.NailsServiceRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NailsServiceService {

    private final NailsServiceRepository nailsServiceRepository;
    private final NailsServiceMapper nailsServiceMapper;
    private final MyRepository myRepository;


    public NailsServiceResponse createService(NailsServiceRequest nailsServiceRequest) {
        checkDuplicate(nailsServiceRequest);
        NailsService nailsServiceToCreate = nailsServiceMapper.map(nailsServiceRequest);
        NailsService nailsServiceToResponse = nailsServiceRepository.save(nailsServiceToCreate);
        return nailsServiceMapper.map(nailsServiceToResponse);
    }


    public List<NailsServiceResponse> allServices() {
        return nailsServiceMapper.map(nailsServiceRepository.findAll());
    }


    public NailsServiceResponse findServiceByID(Integer id) {
        NailsService nailsService = nailsServiceRepository.findById(id).orElseThrow(
                () -> new BusinessException(String.format("The service with id: %s not exist", id))
        );
        return nailsServiceMapper.map(nailsService);
    }


    public List<NailsServiceResponse> findByServiceName(String name) {
        List<NailsService> serviceListFromDB = myRepository.findByServiceName(name);
        return createListOfServiceForResponseFromDB(serviceListFromDB);
    }

    static List<NailsServiceResponse> createListOfServiceForResponseFromDB(List<NailsService> serviceListFromDB) {
        List<NailsServiceResponse> serviceListForResponse = new ArrayList<>();
        for (NailsService nailsService : serviceListFromDB) {
            NailsServiceResponse nailsServiceResponse = new NailsServiceResponse();
            nailsServiceResponse.setId(nailsService.getId());
            nailsServiceResponse.setServiceName(nailsService.getServiceName());
            nailsServiceResponse.setPrice(nailsService.getPrice());
            serviceListForResponse.add(nailsServiceResponse);
        }
        return serviceListForResponse;
    }


    public void updateServicePrice(Integer id, RequestUpdatePrice requestUpdatePrice) {
        NailsService nailsServiceToUpdate = nailsServiceRepository.findById(id).orElseThrow(
                () -> new BusinessException(String.format("The service with id: %s not exist", id))
        );
        nailsServiceToUpdate.setPrice(requestUpdatePrice.getPrice());
    }


    public void updateServiceName(Integer id, RequestUpdateName requestUpdateName) {
        NailsService nailsServiceToUpdate = nailsServiceRepository.findById(id).orElseThrow(
                () -> new BusinessException(String.format("The service with id: %s not exist", id))
        );
        nailsServiceToUpdate.setServiceName(requestUpdateName.getServiceName());
    }

    public void deleteService(Integer id) {
        NailsService nailsServiceToDelete = nailsServiceRepository.findById(id).orElseThrow(
                () -> new BusinessException(String.format("The service with id: %s not exist", id))
        );
        nailsServiceRepository.delete(nailsServiceToDelete);
    }

    public void checkDuplicate(NailsServiceRequest nailsServiceRequest) {
        for (NailsService nailsService : nailsServiceRepository.findAll()) {
            if (nailsService.getServiceName().equals(nailsServiceRequest.getServiceName())) {
                throw new BusinessException("This service already exist");
            }
        }
    }
}
