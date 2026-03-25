import React from 'react';

const ReportTable = ({ reporte }) => {
    const categorias = Object.keys(reporte).filter((key) => key !== 'TOTAL');
    const meses = Object.keys(reporte['TOTAL']).filter((key) => key !== 'TOTAL');

    return (
        <table border="1">
            <thead>
                <tr>
                    <th>Categoría</th>
                    {meses.map((mes) => (
                        <th key={mes}>{mes}</th>
                    ))}
                    <th>Total</th>
                </tr>
            </thead>
            <tbody>
                {categorias.map((categoria) => (
                    <tr key={categoria}>
                        <td>{categoria}</td>
                        {meses.map((mes) => (
                            <td key={mes}>{reporte[categoria][mes].toLocaleString('es-CL')}</td>
                        ))}
                        <td>{reporte[categoria]['TOTAL'].toLocaleString('es-CL')}</td>
                    </tr>
                ))}
                <tr>
                    <td><strong>Total General</strong></td>
                    {meses.map((mes) => (
                        <td key={mes}><strong>{reporte['TOTAL'][mes].toLocaleString('es-CL')}</strong></td>
                    ))}
                    <td><strong>{reporte['TOTAL']['TOTAL'].toLocaleString('es-CL')}</strong></td>
                </tr>
            </tbody>
        </table>
    );
};

export default ReportTable;