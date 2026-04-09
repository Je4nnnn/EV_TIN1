import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid,
  MenuItem,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import {
  createTouristPackage,
  deleteTouristPackage,
  getTouristPackages,
  updateTouristPackage,
  updateTouristPackageAvailability,
} from '../../services/TouristPackageService'

const initialForm = {
  packageName: '',
  description: '',
  destinations: '',
  activities: '',
  extraServices: '',
  daysCount: 1,
  nightsCount: 0,
  roomType: 'Simple',
  transferIncluded: true,
  automobileServiceIncluded: false,
  price: '',
  availableSlots: 0,
  status: 'AVAILABLE',
  available: true,
  maxGuests: 2,
  availableFrom: '',
  availableUntil: '',
}

const TouristPackages = () => {
  const [packages, setPackages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [feedback, setFeedback] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [formData, setFormData] = useState(initialForm)
  const [editingId, setEditingId] = useState(null)

  const loadPackages = async () => {
    setLoading(true)
    setError('')

    try {
      const data = await getTouristPackages(false)
      setPackages(data)
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPackages()
  }, [])

  const openCreateDialog = () => {
    setEditingId(null)
    setFormData(initialForm)
    setDialogOpen(true)
  }

  const openEditDialog = (touristPackage) => {
    setEditingId(touristPackage.id)
    setFormData({
      packageName: touristPackage.packageName,
      description: touristPackage.description,
      destinations: touristPackage.destinations.join(', '),
      activities: touristPackage.activities.join(', '),
      extraServices: touristPackage.extraServices.join(', '),
      daysCount: touristPackage.daysCount,
      nightsCount: touristPackage.nightsCount,
      roomType: touristPackage.roomType,
      transferIncluded: touristPackage.transferIncluded,
      automobileServiceIncluded: touristPackage.automobileServiceIncluded,
      price: touristPackage.price,
      availableSlots: touristPackage.availableSlots,
      status: touristPackage.status,
      available: touristPackage.available,
      maxGuests: touristPackage.maxGuests,
      availableFrom: touristPackage.availableFrom || '',
      availableUntil: touristPackage.availableUntil || '',
    })
    setDialogOpen(true)
  }

  const closeDialog = () => {
    setDialogOpen(false)
    setFormData(initialForm)
  }

  const handleChange = (field, value) => {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const buildPayload = () => ({
    packageName: formData.packageName.trim(),
    description: formData.description.trim(),
    destinations: splitList(formData.destinations),
    activities: splitList(formData.activities),
    extraServices: splitList(formData.extraServices),
    daysCount: Number(formData.daysCount),
    nightsCount: Number(formData.nightsCount),
    roomType: formData.roomType,
    transferIncluded: Boolean(formData.transferIncluded),
    automobileServiceIncluded: Boolean(formData.automobileServiceIncluded),
    price: Number(formData.price),
    availableSlots: Number(formData.availableSlots),
    status: formData.status.trim() || 'AVAILABLE',
    available: Boolean(formData.available),
    maxGuests: Number(formData.maxGuests),
    availableFrom: formData.availableFrom || null,
    availableUntil: formData.availableUntil || null,
  })

  const handleSubmit = async () => {
    try {
      const payload = buildPayload()
      if (editingId) {
        await updateTouristPackage(editingId, payload)
        setFeedback('Paquete turistico actualizado correctamente.')
      } else {
        await createTouristPackage(payload)
        setFeedback('Paquete turistico creado correctamente.')
      }

      closeDialog()
      await loadPackages()
    } catch (submitError) {
      setError(submitError.message)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteTouristPackage(id)
      setFeedback('Paquete turistico eliminado correctamente.')
      await loadPackages()
    } catch (deleteError) {
      setError(deleteError.message)
    }
  }

  const handleToggleAvailability = async (touristPackage) => {
    try {
      await updateTouristPackageAvailability(touristPackage.id, !touristPackage.available)
      setFeedback('Disponibilidad del paquete actualizada.')
      await loadPackages()
    } catch (toggleError) {
      setError(toggleError.message)
    }
  }

  return (
    <Stack spacing={3}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, alignItems: 'center' }}>
        <div>
          <Typography variant="h4" gutterBottom>
            Paquetes turisticos
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Gestiona paquetes complementarios con traslados, destinos, excursiones y cupos.
          </Typography>
        </div>
        <Button variant="contained" onClick={openCreateDialog}>
          Nuevo paquete
        </Button>
      </Box>

      {feedback ? <Alert severity="success">{feedback}</Alert> : null}
      {error ? <Alert severity="error">{error}</Alert> : null}
      {loading ? <CircularProgress /> : null}

      <Grid container spacing={3}>
        {packages.map((touristPackage) => (
          <Grid item xs={12} md={6} lg={4} key={touristPackage.id}>
            <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)', height: '100%' }}>
              <CardContent>
                <Stack spacing={2}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, alignItems: 'flex-start' }}>
                    <Typography variant="h6">{touristPackage.packageName}</Typography>
                    <Chip
                      label={touristPackage.available ? 'Disponible' : 'No disponible'}
                      color={touristPackage.available ? 'success' : 'default'}
                      size="small"
                    />
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {touristPackage.description}
                  </Typography>
                  <Typography variant="body2">
                    Destinos: {touristPackage.destinations.join(', ')}
                  </Typography>
                  <Typography variant="body2">
                    Actividades: {touristPackage.activities.join(', ') || 'Sin actividades adicionales'}
                  </Typography>
                  <Typography variant="body2">
                    Extras: {touristPackage.extraServices.join(', ') || 'Sin extras'}
                  </Typography>
                  <Typography variant="body2">
                    Duracion: {touristPackage.daysCount} dias / {touristPackage.nightsCount} noches
                  </Typography>
                  <Typography variant="body2">Habitacion incluida: {touristPackage.roomType}</Typography>
                  <Typography variant="body2">
                    Cupos: {touristPackage.availableSlots} | Capacidad maxima: {touristPackage.maxGuests}
                  </Typography>
                  <Typography variant="body2">Precio: ${Number(touristPackage.price).toLocaleString('es-CL')}</Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap">
                    {touristPackage.transferIncluded ? <Chip label="Traslado incluido" size="small" /> : null}
                    {touristPackage.automobileServiceIncluded ? <Chip label="Servicio automovil" size="small" /> : null}
                    <Chip label={touristPackage.status} size="small" variant="outlined" />
                  </Stack>
                </Stack>
              </CardContent>
              <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
                <Button size="small" variant="outlined" onClick={() => openEditDialog(touristPackage)}>
                  Editar
                </Button>
                <Button size="small" onClick={() => handleToggleAvailability(touristPackage)}>
                  {touristPackage.available ? 'Desactivar' : 'Activar'}
                </Button>
                <Button size="small" color="error" onClick={() => handleDelete(touristPackage.id)}>
                  Eliminar
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={dialogOpen} onClose={closeDialog} fullWidth maxWidth="md">
        <DialogTitle>{editingId ? 'Editar paquete turistico' : 'Nuevo paquete turistico'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12} md={6}>
              <TextField
                label="Nombre del paquete"
                value={formData.packageName}
                onChange={(event) => handleChange('packageName', event.target.value)}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Estado"
                value={formData.status}
                onChange={(event) => handleChange('status', event.target.value)}
                fullWidth
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Descripcion"
                value={formData.description}
                onChange={(event) => handleChange('description', event.target.value)}
                fullWidth
                multiline
                minRows={3}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                label="Destinos"
                helperText="Separados por coma"
                value={formData.destinations}
                onChange={(event) => handleChange('destinations', event.target.value)}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                label="Actividades"
                helperText="Separadas por coma"
                value={formData.activities}
                onChange={(event) => handleChange('activities', event.target.value)}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                label="Servicios extra"
                helperText="Separados por coma"
                value={formData.extraServices}
                onChange={(event) => handleChange('extraServices', event.target.value)}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField type="number" label="Dias" value={formData.daysCount} onChange={(event) => handleChange('daysCount', event.target.value)} fullWidth />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField type="number" label="Noches" value={formData.nightsCount} onChange={(event) => handleChange('nightsCount', event.target.value)} fullWidth />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                select
                label="Tipo de habitacion"
                value={formData.roomType}
                onChange={(event) => handleChange('roomType', event.target.value)}
                fullWidth
              >
                <MenuItem value="Simple">Simple</MenuItem>
                <MenuItem value="Double">Double</MenuItem>
                <MenuItem value="Suite">Suite</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField type="number" label="Precio" value={formData.price} onChange={(event) => handleChange('price', event.target.value)} fullWidth />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField type="number" label="Cupos disponibles" value={formData.availableSlots} onChange={(event) => handleChange('availableSlots', event.target.value)} fullWidth />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField type="number" label="Capacidad maxima" value={formData.maxGuests} onChange={(event) => handleChange('maxGuests', event.target.value)} fullWidth />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField type="date" label="Disponible desde" value={formData.availableFrom} onChange={(event) => handleChange('availableFrom', event.target.value)} InputLabelProps={{ shrink: true }} fullWidth />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField type="date" label="Disponible hasta" value={formData.availableUntil} onChange={(event) => handleChange('availableUntil', event.target.value)} InputLabelProps={{ shrink: true }} fullWidth />
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControlLabel control={<Switch checked={formData.available} onChange={(event) => handleChange('available', event.target.checked)} />} label="Disponible" />
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControlLabel control={<Switch checked={formData.transferIncluded} onChange={(event) => handleChange('transferIncluded', event.target.checked)} />} label="Incluye traslado" />
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControlLabel control={<Switch checked={formData.automobileServiceIncluded} onChange={(event) => handleChange('automobileServiceIncluded', event.target.checked)} />} label="Incluye automovil" />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog}>Cancelar</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingId ? 'Guardar cambios' : 'Crear paquete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}

const splitList = (value) =>
  value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

export default TouristPackages
