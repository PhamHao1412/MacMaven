package WebProject.WebProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import WebProject.WebProject.entity.Order;
import WebProject.WebProject.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
	@Query("SELECT u FROM User u Where u.email = :email")
	User findByEmail(@Param("email") String email);
	@Query("SELECT u FROM User u Where u.id = :username")
	User findByUsername(@Param("username") String username);
	User findById(String id);
	
//	@Query(value="select * from user u where u.id = ?1 and u.role = ?2",nativeQuery = true)
//	@Query("SELECT u FROM User u Where u.id = :id and u.role = :role")
	User findByIdAndRole(@Param("id") String id, @Param("role")String role);
	
	void deleteById(String id);

}
