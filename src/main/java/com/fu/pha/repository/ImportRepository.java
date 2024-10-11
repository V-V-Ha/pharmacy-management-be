package com.fu.pha.repository;

import com.fu.pha.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> {

    @Query("SELECT i.invoiceNumber FROM Import i ORDER BY i.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

}
