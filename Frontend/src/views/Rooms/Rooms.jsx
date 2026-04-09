import { Stack, Typography } from '@mui/material'
import RoomsTable from '../../components/RoomsTable'

const Rooms = () => {
  return (
    <Stack spacing={3}>
      <div>
        <Typography variant="h4" gutterBottom>
          Habitaciones
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Vista operacional del inventario y estado actual de las habitaciones.
        </Typography>
      </div>
      <RoomsTable />
    </Stack>
  )
}

export default Rooms
