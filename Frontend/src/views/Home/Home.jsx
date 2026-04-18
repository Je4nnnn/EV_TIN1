import { useEffect, useState } from 'react'
import dayjs from 'dayjs'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  MenuItem,
  ToggleButton,
  ToggleButtonGroup,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import CalendarHome from '../../components/CalendarHome'
import TablePrices from '../../components/TablePrices'
import { useNavigate } from 'react-router-dom'
import { getTouristPackages } from '../../services/TouristPackageService'
import { getAvailableRooms } from '../../services/RoomsService'

const STAY_TYPES = [
  { value: 'Manana', label: 'Turno manana (08:30 - 18:30)', checkoutDays: 0 },
  { value: 'Noche', label: 'Turno noche (18:30 - 08:30)', checkoutDays: 1 },
  { value: 'Completo', label: 'Estadia completa (08:30 - 08:30)', checkoutDays: 1 },
]

const ROOM_TYPES = [
  { value: 'Simple', label: 'Habitacion simple' },
  { value: 'Double', label: 'Habitacion doble' },
  { value: 'Suite', label: 'Suite' },
]

const Home = () => {
  const [reservationMode, setReservationMode] = useState('manual')
  const [checkInDate, setCheckInDate] = useState(null)
  const [checkOutDate, setCheckOutDate] = useState(null)
  const [stayType, setStayType] = useState('')
  const [roomType, setRoomType] = useState('')
  const [touristPackages, setTouristPackages] = useState([])
  const [selectedPackageId, setSelectedPackageId] = useState('')
  const [availableRooms, setAvailableRooms] = useState([])
  const [selectedRoomId, setSelectedRoomId] = useState('')
  const [conflicto, setConflicto] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const navigate = useNavigate()

  useEffect(() => {
    const fetchPackages = async () => {
      try {
        const data = await getTouristPackages(true)
        setTouristPackages(data)
      } catch (error) {
        setFeedback({ type: 'error', message: error.message })
      }
    }

    fetchPackages()
  }, [])

  useEffect(() => {
    if (reservationMode !== 'manual') {
      return
    }

    if (!checkInDate || !stayType) {
      return
    }

    const selectedStayType = STAY_TYPES.find((type) => type.value === stayType)
    const checkout = checkInDate.add(selectedStayType?.checkoutDays || 0, 'day')
    setCheckOutDate(checkout)
    verifyConflict()
  }, [checkInDate, stayType])

  useEffect(() => {
    if (reservationMode !== 'package' || !selectedPackageId) {
      return
    }

    const selectedPackage = touristPackages.find((item) => String(item.id) === String(selectedPackageId))
    if (!selectedPackage) {
      return
    }

    if (!selectedPackage.availableFrom || !selectedPackage.availableUntil) {
      setFeedback({
        type: 'error',
        message: 'El paquete seleccionado no tiene fechas configuradas y no se puede reservar.',
      })
      return
    }

    const packageCheckIn = dayjs(selectedPackage.availableFrom)
    const packageCheckOut = dayjs(selectedPackage.availableUntil)
    setCheckInDate(packageCheckIn)
    setCheckOutDate(packageCheckOut)
    setRoomType(selectedPackage.roomType)
    setStayType('Completo')
    verifyConflict()
  }, [reservationMode, selectedPackageId, touristPackages])

  useEffect(() => {
    const loadAvailableRooms = async () => {
      if (!checkInDate || !checkOutDate || !roomType || !stayType) {
        setAvailableRooms([])
        setSelectedRoomId('')
        return
      }

      try {
        const rooms = await getAvailableRooms({
          checkInDate: checkInDate.format('YYYY-MM-DD'),
          checkOutDate: checkOutDate.format('YYYY-MM-DD'),
          roomType,
          stayType,
        })

        setAvailableRooms(rooms)

        if (!rooms.some((room) => String(room.id) === String(selectedRoomId))) {
          setSelectedRoomId(rooms[0]?.id ? String(rooms[0].id) : '')
        }

        if (rooms.length === 0) {
          setConflicto(true)
          setFeedback({
            type: 'warning',
            message: 'No hay habitaciones disponibles para ese rango y tipo seleccionado.',
          })
          return
        }

        setConflicto(false)
        setFeedback({ type: '', message: '' })
      } catch (error) {
        setAvailableRooms([])
        setSelectedRoomId('')
        setFeedback({ type: 'error', message: error.message })
      }
    }

    loadAvailableRooms()
  }, [checkInDate, checkOutDate, roomType, stayType])

  const handleCheckOutChange = (event) => {
    const newDate = dayjs(event.target.value)
    setCheckOutDate(newDate)
    verifyConflict()
  }

  const verifyConflict = () => {
    setConflicto(false)
  }

  const handleContinue = () => {
    if (reservationMode === 'package' && !selectedPackageId) {
      setFeedback({ type: 'error', message: 'Selecciona un paquete turistico antes de continuar.' })
      return
    }

    if (!checkInDate || !checkOutDate || !stayType || !roomType || !selectedRoomId) {
      setFeedback({ type: 'error', message: 'Completa todos los campos antes de continuar.' })
      return
    }

    if (conflicto) {
      setFeedback({
        type: 'error',
        message: 'Debes cambiar la fecha o el tipo de estancia para evitar conflicto.',
      })
      return
    }

    const selectedRoom = availableRooms.find((room) => String(room.id) === String(selectedRoomId))
    const selectedPackage =
      reservationMode === 'package'
        ? touristPackages.find((item) => String(item.id) === String(selectedPackageId))
        : null

    navigate('/formulario', {
      state: {
        dia: checkInDate.format('YYYY-MM-DD'),
        diaSalida: checkOutDate.format('YYYY-MM-DD'),
        tipoDuracion: roomType,
        tipoEstancia: stayType,
        roomId: Number(selectedRoomId),
        roomNumber: selectedRoom?.roomNumber || '',
        paqueteTuristico: selectedPackage || null,
      },
    })
  }

  const handleReservationModeChange = (_event, newValue) => {
    if (!newValue) {
      return
    }

    setReservationMode(newValue)
    setFeedback({ type: '', message: '' })
    setConflicto(false)
    setSelectedPackageId('')
    setAvailableRooms([])
    setSelectedRoomId('')
    setCheckInDate(null)
    setCheckOutDate(null)
    setRoomType('')
    setStayType('')
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h3" gutterBottom>
          Gestion de reservas hoteleras
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Selecciona fechas, revisa precios y valida disponibilidad antes de registrar la reserva.
        </Typography>
      </Box>

      {feedback.message ? <Alert severity={feedback.type || 'info'}>{feedback.message}</Alert> : null}

      <Grid container spacing={3}>
        <Grid item xs={12} lg={7}>
          <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
            <CardContent>
              <Stack spacing={2.5}>
                <Typography variant="h5">Seleccion de estadia</Typography>
                <ToggleButtonGroup
                  value={reservationMode}
                  exclusive
                  onChange={handleReservationModeChange}
                  color="primary"
                >
                  <ToggleButton value="manual">Reserva manual</ToggleButton>
                  <ToggleButton value="package">Desde paquete</ToggleButton>
                </ToggleButtonGroup>

                {reservationMode === 'manual' ? (
                  <>
                    <CalendarHome selectedDate={checkInDate} setSelectedDate={setCheckInDate} />
                    <TextField
                      select
                      label="Tipo de habitacion"
                      value={roomType}
                      onChange={(event) => setRoomType(event.target.value)}
                      fullWidth
                    >
                      {ROOM_TYPES.map((room) => (
                        <MenuItem key={room.value} value={room.value}>
                          {room.label}
                        </MenuItem>
                      ))}
                    </TextField>
                    <TextField
                      select
                      label="Tipo de estancia"
                      value={stayType}
                      onChange={(event) => setStayType(event.target.value)}
                      fullWidth
                    >
                      {STAY_TYPES.map((stay) => (
                        <MenuItem key={stay.value} value={stay.value}>
                          {stay.label}
                        </MenuItem>
                      ))}
                    </TextField>
                  </>
                ) : (
                  <>
                    <TextField
                      select
                      label="Paquete turistico"
                      value={selectedPackageId}
                      onChange={(event) => setSelectedPackageId(event.target.value)}
                      fullWidth
                    >
                      {touristPackages.map((touristPackage) => (
                        <MenuItem key={touristPackage.id} value={touristPackage.id}>
                          {touristPackage.packageName}
                        </MenuItem>
                      ))}
                    </TextField>
                    {selectedPackageId ? (
                      <Card variant="outlined" sx={{ borderColor: 'rgba(15, 118, 110, 0.24)' }}>
                        <CardContent>
                          <Stack spacing={1.5}>
                            {(() => {
                              const selectedPackage = touristPackages.find(
                                (item) => String(item.id) === String(selectedPackageId),
                              )

                              if (!selectedPackage) {
                                return null
                              }

                              return (
                                <>
                                  <Typography variant="h6">{selectedPackage.packageName}</Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    {selectedPackage.description}
                                  </Typography>
                                  <Stack direction="row" spacing={1} flexWrap="wrap">
                                    <Chip label={`Habitacion ${selectedPackage.roomType}`} size="small" />
                                    <Chip label={`${selectedPackage.daysCount} dias`} size="small" />
                                    <Chip label={`${selectedPackage.availableSlots} cupos`} size="small" />
                                  </Stack>
                                  <Typography variant="body2">
                                    Fechas reservadas: {selectedPackage.availableFrom} al {selectedPackage.availableUntil}
                                  </Typography>
                                </>
                              )
                            })()}
                          </Stack>
                        </CardContent>
                      </Card>
                    ) : null}
                  </>
                )}
                {checkOutDate ? (
                  <Stack spacing={1.5}>
                    <Typography variant="body1">
                      Check-in: <strong>{checkInDate?.format('DD/MM/YYYY')}</strong>
                    </Typography>
                    <Typography variant="body1">
                      Check-out: <strong>{checkOutDate.format('DD/MM/YYYY')}</strong>
                    </Typography>
                    {reservationMode === 'manual' ? (
                      <TextField
                        type="date"
                        label="Ajustar fecha de salida"
                        value={checkOutDate.format('YYYY-MM-DD')}
                        onChange={handleCheckOutChange}
                        InputLabelProps={{ shrink: true }}
                        inputProps={{
                          min: checkInDate ? checkInDate.format('YYYY-MM-DD') : undefined,
                        }}
                      />
                    ) : null}
                  </Stack>
                ) : null}
                {availableRooms.length > 0 ? (
                  <TextField
                    select
                    label="Habitacion disponible"
                    value={selectedRoomId}
                    onChange={(event) => setSelectedRoomId(event.target.value)}
                    fullWidth
                  >
                    {availableRooms.map((room) => (
                      <MenuItem key={room.id} value={room.id}>
                        {room.roomNumber} - {room.type}
                      </MenuItem>
                    ))}
                  </TextField>
                ) : null}
                <Button variant="contained" size="large" onClick={handleContinue}>
                  Continuar con reserva
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} lg={5}>
          <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)', height: '100%' }}>
            <CardContent>
              <Typography variant="h5" sx={{ mb: 2 }}>
                Tarifas de referencia
              </Typography>
              <TablePrices />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Stack>
  )
}

export default Home
