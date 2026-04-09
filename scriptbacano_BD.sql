-- Consultas utiles para inspeccionar la base hotelrm en PostgreSQL
-- Ejecutar en pgAdmin sobre la base hotelrm

-- 1. Ver todas las tablas del esquema public
SELECT
    schemaname,
    tablename,
    tableowner
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

-- 2. Ver columnas, tipos y nulabilidad de todas las tablas
SELECT
    table_name,
    ordinal_position,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- 3. Ver claves foraneas del esquema
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- 4. Cantidad de registros por tabla
SELECT 'users' AS table_name, COUNT(*) AS total FROM public.users
UNION ALL
SELECT 'rooms', COUNT(*) FROM public.rooms
UNION ALL
SELECT 'reservations', COUNT(*) FROM public.reservations
UNION ALL
SELECT 'reservation_details', COUNT(*) FROM public.reservation_details
UNION ALL
SELECT 'tourist_packages', COUNT(*) FROM public.tourist_packages
ORDER BY table_name;

-- 5. Usuarios
SELECT
    id,
    rut,
    name,
    email,
    phone_number,
    date_birthday,
    category_frecuency,
    number_visits
FROM public.users
ORDER BY id;

-- 6. Habitaciones
SELECT
    id,
    room_number,
    type,
    status
FROM public.rooms
ORDER BY room_number;

-- 7. Reservas operativas (no canceladas)
SELECT
    r.id,
    r.reservation_code,
    u.name AS cliente,
    u.rut,
    r.room_number,
    r.room_type,
    r.stay_type,
    r.check_in_date,
    r.check_out_date,
    r.number_of_guests,
    r.final_amount,
    r.tourist_package_name
FROM public.reservations r
JOIN public.users u ON u.id = r.client_id
WHERE COALESCE(r.cancelled, FALSE) = FALSE
ORDER BY r.check_in_date, r.room_number;

-- 8. Reservas canceladas
SELECT
    r.id,
    r.reservation_code,
    u.name AS cliente,
    u.rut,
    r.room_number,
    r.room_type,
    r.stay_type,
    r.check_in_date,
    r.check_out_date,
    r.final_amount,
    r.cancelled,
    r.cancelled_at,
    r.tourist_package_name
FROM public.reservations r
JOIN public.users u ON u.id = r.client_id
WHERE COALESCE(r.cancelled, FALSE) = TRUE
ORDER BY r.cancelled_at DESC NULLS LAST, r.id DESC;

-- 9. Detalles de reserva con huespedes
SELECT
    rd.id,
    rd.reservation_id,
    r.reservation_code,
    rd.guest_name,
    rd.user_id,
    rd.discount,
    rd.final_amount
FROM public.reservation_details rd
JOIN public.reservations r ON r.id = rd.reservation_id
ORDER BY rd.reservation_id, rd.id;

-- 10. Paquetes turisticos
SELECT
    id,
    package_name,
    room_type,
    days_count,
    nights_count,
    price,
    available_slots,
    available,
    status,
    available_from,
    available_until
FROM public.tourist_packages
ORDER BY package_name;

-- 11. Reservas activas por titular (para revisar el limite de 3)
SELECT
    u.rut,
    u.name,
    COUNT(*) AS active_reservations
FROM public.reservations r
JOIN public.users u ON u.id = r.client_id
WHERE COALESCE(r.cancelled, FALSE) = FALSE
  AND (
      CASE
          WHEN LOWER(r.stay_type) = 'manana' THEN (r.check_out_date + TIME '18:30')
          WHEN LOWER(r.stay_type) = 'noche' THEN (r.check_out_date + TIME '08:30')
          WHEN LOWER(r.stay_type) = 'completo' THEN (r.check_out_date + TIME '08:30')
          ELSE (r.check_out_date + TIME '12:00')
      END
  ) >= NOW()
GROUP BY u.rut, u.name
ORDER BY active_reservations DESC, u.name;

-- 12. Uso de habitaciones por fecha
SELECT
    r.room_number,
    r.room_type,
    r.check_in_date,
    r.check_out_date,
    r.stay_type,
    r.reservation_code,
    u.name AS cliente,
    u.rut
FROM public.reservations r
JOIN public.users u ON u.id = r.client_id
WHERE COALESCE(r.cancelled, FALSE) = FALSE
ORDER BY r.room_number, r.check_in_date;

-- 13. Buscar reservas de un RUT especifico
-- Reemplaza '11111111K' por el RUT deseado
SELECT
    r.id,
    r.reservation_code,
    u.name,
    u.rut,
    r.room_number,
    r.room_type,
    r.stay_type,
    r.check_in_date,
    r.check_out_date,
    r.cancelled,
    r.cancelled_at
FROM public.reservations r
JOIN public.users u ON u.id = r.client_id
WHERE u.rut = '11111111K'
ORDER BY r.check_in_date DESC, r.id DESC;

-- 14. Buscar reservas de una habitacion especifica
-- Reemplaza 'D001' por la habitacion deseada
SELECT
    r.id,
    r.reservation_code,
    r.room_number,
    r.room_type,
    r.stay_type,
    r.check_in_date,
    r.check_out_date,
    r.cancelled,
    r.cancelled_at
FROM public.reservations r
WHERE r.room_number = 'D001'
ORDER BY r.check_in_date DESC, r.id DESC;
