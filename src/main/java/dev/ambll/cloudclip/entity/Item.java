package dev.ambll.cloudclip.entity;

import dev.ambll.cloudclip.enums.ItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type;
    @Column(name = "text")
    private String text;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
