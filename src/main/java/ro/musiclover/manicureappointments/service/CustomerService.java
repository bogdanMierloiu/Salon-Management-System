package ro.musiclover.manicureappointments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.musiclover.manicureappointments.entity.Appointment;
import ro.musiclover.manicureappointments.entity.Customer;
import ro.musiclover.manicureappointments.entity.NailsCare;
import ro.musiclover.manicureappointments.exception.BusinessException;
import ro.musiclover.manicureappointments.mapper.CustomerMapper;
import ro.musiclover.manicureappointments.model.appointment.AppointmentResponseForCustomerDetail;
import ro.musiclover.manicureappointments.model.customer.*;
import ro.musiclover.manicureappointments.model.nails_services.NailsServiceForCustomerDetail;
import ro.musiclover.manicureappointments.repository.CustomerRepository;
import ro.musiclover.manicureappointments.repository.MyRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;


    public CustomerResponse createCustomer(CustomerRequest customerRequest) {
        checkDuplicate(customerRequest);
        validatePhoneNumber(customerRequest.getPhoneNumber());
        Customer customer = customerMapper.map(customerRequest);
        return customerMapper.map(customerRepository.save(customer));
    }


    public List<CustomerResponse> getAllCustomers() {
        return customerMapper.map(customerRepository.findAll());
    }


    public List<CustomerResponse> getAllActiveCustomers() {
        List<Customer> customersFromDb = customerRepository.findAll();
        List<CustomerResponse> customersForResponse = new ArrayList<>();
        for (Customer customer : customersFromDb) {
            if (customer.getActive()) {
                customersForResponse.add(customerMapper.map(customer));
            }
        }
        return customersForResponse;
    }


    public CustomerResponse findCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", id))
        );
        return customerMapper.map(customer);
    }


    public CustomerDetailResponse findByIdWithDetails(Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException(
                "CustomerWebController not found"));

        return createCustomerDetailsResponse(customer);
    }


    public CustomerResponse findByFirstName(String firstName) {
        if (firstName.isBlank()) {
            throw new IllegalArgumentException("Invalid name");
        }
        Customer customer = customerRepository.findByFirstName(firstName);
        return customerMapper.map(customer);
    }

    public CustomerDetailResponse customerWithAppointments(Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException(
                "CustomerWebController not found"));
        return createCustomerDetailsResponse(customer);
    }

    private CustomerDetailResponse createCustomerDetailsResponse(Customer customer) {
        CustomerDetailResponse customerDetailResponse = new CustomerDetailResponse();
        customerDetailResponse.setFirstName(customer.getFirstName());
        customerDetailResponse.setLastName(customer.getLastName());
        customerDetailResponse.setAppointments(new ArrayList<>());    // aici este gol
        for (Appointment appointment : customer.getAppointments()) {
            AppointmentResponseForCustomerDetail appointmentResponseForCustomerDetail = new AppointmentResponseForCustomerDetail();
            appointmentResponseForCustomerDetail.setAppointmentDateTime(appointment.getAppointmentDateTime());
            appointmentResponseForCustomerDetail.setNailsServices(new ArrayList<>());
            for (NailsCare nailsCare : appointment.getNailsCares()) {
                NailsServiceForCustomerDetail nailsServiceForCustomerDetail = new NailsServiceForCustomerDetail();
                nailsServiceForCustomerDetail.setServiceName(nailsCare.getServiceName());
                appointmentResponseForCustomerDetail.getNailsServices().add(nailsServiceForCustomerDetail);
            }
            customerDetailResponse.getAppointments().add(appointmentResponseForCustomerDetail);
        }
        return customerDetailResponse;
    }


    public void updateCustomer(CustomerRequest customerRequest) {

        validatePhoneNumber(customerRequest.getPhoneNumber());
        Customer customerToUpdate = customerRepository.findById(customerRequest.getId()).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", customerRequest.getId())
                )
        );
        if (!(customerToUpdate == null)) {
            customerToUpdate.setFirstName(
                    customerRequest.getFirstName() != null ? customerRequest.getFirstName() : customerToUpdate.getFirstName());
            customerToUpdate.setLastName(
                    customerRequest.getLastName() != null ? customerRequest.getLastName() : customerToUpdate.getLastName());
            if (customerRequest.getPhoneNumber() != null) {
                validatePhoneNumber(customerRequest.getPhoneNumber());
                customerToUpdate.setPhoneNumber(customerRequest.getPhoneNumber());
            } else {
                customerToUpdate.setPhoneNumber(customerToUpdate.getPhoneNumber());
            }
            customerToUpdate.setBirthDate(
                    customerRequest.getBirthDate() != null ? customerRequest.getBirthDate() : customerToUpdate.getBirthDate());
            customerToUpdate.setEmail(
                    customerRequest.getEmail() != null ? customerRequest.getEmail() : customerToUpdate.getEmail());
            customerToUpdate.setActive(
                    customerRequest.getActive() != null ? customerRequest.getActive() : customerToUpdate.getActive());
        }

    }

    public void updateFirstLastName(Integer id, RequestUpdateNameCustomer customerRequest) {
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", id)
                )
        );
        customerToUpdate.setFirstName(customerRequest.getFirstName());
        customerToUpdate.setLastName(customerRequest.getLastName());
    }


    public void updateStatus(Integer id, CustomerUpdateStatus customerUpdateStatus) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException("Not found")
        );
        customer.setActive(customerUpdateStatus.getActive().equals("true"));
    }


    public void updatePhoneNumber(Integer id, RequestUpdatePhoneNumberCustomer request) {
        validatePhoneNumber(request.getPhoneNumber());
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", id)
                )
        );
        customerToUpdate.setPhoneNumber(request.getPhoneNumber());
    }


    public void updateBirthDate(Integer id, RequestUpdateBirthDateCustomer request) {
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", id)
                )
        );
        customerToUpdate.setBirthDate(request.getBirthDate());
    }


    public void updateEmail(Integer id, RequestUpdateEmailCustomer request) {
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
                () -> new BusinessException(
                        String.format("CustomerWebController with id: %s not found", id)
                )
        );
        customerToUpdate.setEmail(request.getEmail());
    }


    public void deleteById(Integer id) {
        Customer customerToDelete = customerRepository.findById(id).orElseThrow(() ->
                new BusinessException("Not found"));
        customerRepository.deleteById(customerToDelete.getId());
    }

    public void checkDuplicate(CustomerRequest customerRequest) {
        for (Customer customer : customerRepository.findAll()) {
            if (customer.getFirstName().equals(customerRequest.getFirstName()) &&
                    customer.getLastName().equals(customerRequest.getLastName())) {
                throw new BusinessException("This CustomerWebController already exist");
            }
        }
    }

    public void validatePhoneNumber(String string) {
        if (string.isBlank() ||
                !string.matches("[0-9]+") ||
                string.length() < 10) {
            throw new BusinessException("Invalid phone number. Try again -> only with digits and minimum 10");
        }
    }

    public int getAge(int id) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        return (int) ChronoUnit.YEARS.between(customer.getBirthDate(), LocalDate.now());
    }

    public int under22() {
        int total = 0;
        for (Customer customer : customerRepository.findAll()) {
            if (getAge(customer.getId()) <= 22) {
                total += 1;
            }
        }
        return total;
    }

    public int between23and30() {
        int total = 0;
        for (Customer customer : customerRepository.findAll()) {
            if (getAge(customer.getId()) > 22 && getAge(customer.getId()) <= 30) {
                total += 1;
            }
        }
        return total;
    }

    public int between31and40() {
        int total = 0;
        for (Customer customer : customerRepository.findAll()) {
            if (getAge(customer.getId()) > 30 && getAge(customer.getId()) <= 40) {
                total += 1;
            }
        }
        return total;
    }

    public int olderThan41() {
        int total = 0;
        for (Customer customer : customerRepository.findAll()) {
            if (getAge(customer.getId()) > 40) {
                total += 1;
            }
        }
        return total;
    }


}
