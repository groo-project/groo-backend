package com.x1.groo.email.repository;

import com.x1.groo.email.aggregate.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, Integer> {


}
