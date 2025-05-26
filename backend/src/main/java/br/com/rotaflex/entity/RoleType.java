package br.com.rotaflex.entity;

public enum RoleType {
    ADMIN(1L, "ADMIN"),
    ADMINISTRATIVO(2L, "ADMINISTRATIVO");

    private final Long id;
    private final String nome;

    RoleType(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    // 🔥 Este é o método que estava faltando!
    public static RoleType fromId(Long id) {
        for (RoleType type : RoleType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("ID de RoleType inválido: " + id);
    }

    public static RoleType fromNome(String nome) {
        for (RoleType type : RoleType.values()) {
            if (type.getNome().equalsIgnoreCase(nome)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Nome de RoleType inválido: " + nome);
    }
}
