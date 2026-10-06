package dev.ambll.cloudclip.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "room_key", nullable = false, unique = true)
    private String roomKey;
    @Column(name = "name")
    private String name;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "owner_password_hash", nullable = false)
    private String ownerPasswordHash;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "room")
    private List<Item> items = new ArrayList<>();
}
