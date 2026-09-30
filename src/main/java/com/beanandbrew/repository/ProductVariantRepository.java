package com.beanandbrew.repository;

import com.beanandbrew.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :qty WHERE v.id = :id")
    int addStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stock = :qty WHERE v.id = :id")
    int setStock(@Param("id") Long id, @Param("qty") int qty);
}