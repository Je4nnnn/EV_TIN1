import React, { useState, useEffect } from 'react';
import { fetchReporteIngresos, fetchReporteParticipantes } from '../../services/ReportsService';
import ReportTable from '../../components/ReportTable';

const Reports = () => {
    const [reporte, setReporte] = useState(null);
    const [reporteParticipantes, setReporteParticipantes] = useState(null);
    const [fechaInicio, setFechaInicio] = useState('2025-01-01');
    const [fechaFin, setFechaFin] = useState('2025-03-31');

    const fetchReporte = async () => {
        try {
            const data = await fetchReporteIngresos(fechaInicio, fechaFin);
            setReporte(data);
        } catch (error) {
            console.error('Error al obtener el reporte de ingresos:', error);
        }
    };

    const fetchReporteParticipantesData = async () => {
        try {
            const data = await fetchReporteParticipantes(fechaInicio, fechaFin);
            setReporteParticipantes(data);
        } catch (error) {
            console.error('Error al obtener el reporte de participantes:', error);
        }
    };

    useEffect(() => {
        fetchReporte();
        fetchReporteParticipantesData();
    }, [fechaInicio, fechaFin]);

    return (
        <div style={{ backgroundColor: 'white', padding: '20px' }}>
            <div>
                <label>Fecha Inicio:</label>
                <input
                    type="date"
                    value={fechaInicio}
                    onChange={(e) => setFechaInicio(e.target.value)}
                />
                <label>Fecha Fin:</label>
                <input
                    type="date"
                    value={fechaFin}
                    onChange={(e) => setFechaFin(e.target.value)}
                />
                <h1>Reporte de Ingresos</h1>
            </div>
            {reporte ? (
                <ReportTable reporte={reporte} />
            ) : (
                <p>No hay datos disponibles para el rango de fechas seleccionado.</p>
            )}
            <h2>Reporte de Participantes</h2>
            {reporteParticipantes ? (
                <ReportTable reporte={reporteParticipantes} />
            ) : (
                <p>No hay datos disponibles para la cantidad de participantes en el rango de fechas seleccionado.</p>
            )}
        </div>
    );
};

export default Reports;