```oracle
CREATE OR REPLACE FUNCTION sani_distance_sphere (jin1 number(22,6),
            wei1 number(22,6),
            jin2 number(22,6),
            wei2 number(22,6)) return number(22,6) is
             j1 number(22,6) ;
             w1 number(22,6) ;

             j2 number(22,6) ;
             w2 number(22,6) ;
             R number(22,6) ;
             pi number(22,6);
begin
            SELECT ACOS(-1) into pi FROM DUAL;
            IF jin1 >= 0 and jin1 <= 180 and wei1 <= 90 and wei1 >= 0 and jin2 >= 0 and jin2 <= 180 and wei2 >= 0 and wei2 <= 90 THEN
             j1 := jin1 * pi / 180 ;
             w1 := wei1 * pi / 180 ;
             j2 := jin2 * pi / 180 ;
             w2 := wei2 * pi / 180 ;
            R := 6370986 ; RETURN R * acos(cos(w1) * cos(w2) * cos(j1 - j2) + sin(w1) * sin(w2)) ;
            ELSE
            RETURN POWER(POWER(jin2 - jin1, 2) + POWER(wei2 - wei1, 2),0.5) ;
            END
            IF ;
end sani_distance_sphere;
/
```