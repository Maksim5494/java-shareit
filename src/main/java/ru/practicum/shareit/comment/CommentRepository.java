package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItem_Id(Long itemId);

    List<Comment> findAllByItem_In(List<Item> items);

}
