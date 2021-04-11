package jdbc_database;

public interface DB {
    String TActividadesConDias = "actividades NATURAL JOIN horarios NATURAL JOIN dias_horarios";
    String TActividadesConMenbresias = "actividades_menbresias NATURAL JOIN menbresias";
    String TactividadesConUsuarios = "actividades_menbresias natural join menbresias natural join usuarios";
    String CGroupDias = "GROUP_CONCAT(dia SEPARATOR ', ') AS dias";
}
