import React from 'react';
import FullCalendar from '@fullcalendar/react';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

const WeeklyCalendar = ({ events }) => {
    const renderEventContent = (eventInfo) => {
        console.log('Contenido del evento:', eventInfo.event.extendedProps); 
        return (
          <div>
            <b>{eventInfo.timeText}</b> 
            <div>{eventInfo.event.extendedProps.codigo_reserva}</div> 
          </div>
        );
      };

  return (
    <div style={{ height: '800px', width: '100%' }}>
        <FullCalendar
        plugins={[timeGridPlugin, interactionPlugin]}
        initialView="timeGridWeek"
        events={events} 
        editable={false}
        selectable={false}
        eventContent={renderEventContent} 
        headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: 'timeGridWeek,timeGridDay',
        }}
        slotMinTime="10:00:00" 
        slotMaxTime="22:00:00"
        />
    </div>
  );
};

export default WeeklyCalendar;